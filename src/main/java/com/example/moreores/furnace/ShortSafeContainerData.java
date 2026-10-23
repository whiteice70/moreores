package com.example.moreores.furnace;

import net.minecraft.world.inventory.ContainerData;

/**
 * 给 {@link ContainerData} 套一层「按比例缩短」，让超长的燃料时长也能通过同步包。
 *
 * <h2>为什么需要它</h2>
 *
 * 熔炉界面里那团火焰的高度是 {@code 剩余燃烧时间 / 总燃烧时间}
 * （见 {@code AbstractFurnaceMenu#getLitProgress}），这对值靠
 * {@code ClientboundContainerSetDataPacket} 从服务端同步到客户端。
 *
 * <p>而那个包的编解码是（反编译 26.3 的结果）：
 *
 * <pre>
 * write: {@code buf.writeShort(this.value);}      // ← 短整型！
 * read:  {@code this.value = buf.readShort();}
 * </pre>
 *
 * 也就是<b>只能传 [-32768, 32767]</b>。原版所有燃料都 ≤ 20000（岩浆桶 20000、
 * 煤炭块 16000），所以一直没人踩到这个坑；但铀锭是 200000 tick，截断之后
 *
 * <pre>200000 mod 65536 = 3392</pre>
 *
 * 客户端于是以为「总共只需要烧 3392 tick」，火焰 2.8 分钟就排空了，
 * 而服务端实际要烧 2.8 小时 —— 这就是「火焰时间明显小于燃烧时间」的由来。
 *
 * <h2>怎么修</h2>
 *
 * 火焰只看<b>比值</b>，所以把「剩余」和「总时长」<b>同时</b>除以一个系数即可：
 * 比值不变，数值又能落进 short 的范围。
 *
 * <p>系数按总时长自适应 —— {@code 总时长 ≤ 32767} 时系数为 1，
 * 也就是<b>原版燃料和高能熔炉完全不受影响</b>（高能熔炉是 20000）。
 *
 * <p>系数只由<b>总时长</b>决定，而总时长在一整轮燃烧里是常数，
 * 所以「剩余 / 总」的比值不会被破坏。
 *
 * <p>⚠️ 缩短只在<b>同步出去的那一份</b>上做（{@code addDataSlots} 的参数），
 * 方块实体自己用的还是原始值，所以<b>实际燃烧时间一点没变</b>。
 */
public final class ShortSafeContainerData implements ContainerData {
	/** {@code FriendlyByteBuf#writeShort} 能表达的正数上限。 */
	private static final int MAX_SYNCABLE = Short.MAX_VALUE;

	/** 需要缩写的两个索引：0 = 剩余燃烧时间，1 = 总燃烧时间。 */
	private static final int LIT_TIME = 0;
	private static final int LIT_TOTAL_TIME = 1;

	private final ContainerData delegate;

	public ShortSafeContainerData(ContainerData delegate) {
		this.delegate = delegate;
	}

	@Override
	public int get(int index) {
		int value = this.delegate.get(index);

		if (index != LIT_TIME && index != LIT_TOTAL_TIME) {
			// 烧制进度（索引 2 / 3）本来就小（几百 tick），不碰，免得箭头变卡顿
			return value;
		}

		int divisor = divisor();

		// 除数取整会丢尾数，剩最后几 tick 时火焰会提前灭 —— 最多差 1 tick 量级，可以忽略
		return divisor == 1 ? value : value / divisor;
	}

	@Override
	public void set(int index, int value) {
		// 客户端收到的已经是缩短后的值，原样存进底层即可
		this.delegate.set(index, value);
	}

	@Override
	public int getCount() {
		return this.delegate.getCount();
	}

	/**
	 * 当前该除以几。
	 *
	 * <p>以「总燃烧时间」为准 —— 它在一整轮里不变，所以剩余时间那条也不会忽大忽小。
	 *
	 * <p>这个式子天然是幂等的：缩短过一次之后总值必定 ≤ 32767，
	 * 于是下次算出来除数是 1，不会反复缩。
	 */
	private int divisor() {
		if (this.delegate.getCount() <= LIT_TOTAL_TIME) {
			return 1;
		}

		int total = this.delegate.get(LIT_TOTAL_TIME);

		if (total <= MAX_SYNCABLE) {
			return 1;
		}

		return (total + MAX_SYNCABLE - 1) / MAX_SYNCABLE;
	}
}
