# -*- coding: utf-8 -*-
"""生成模组的全部贴图：6 张物品图 + 3 张金属块图。

**为什么要有这个脚本**

项目的贴图规矩是「一物一脸」：形状和明暗要能自己说明它是什么，
不能靠给原版图标换个色相了事。手写 16×16 的像素表错一位根本看不出来，
所以形状、配色、描边都写成代码，改一处随手重跑。

---
## 一、物品（`textures/item/`）

六张图原来的问题正好是两类：

* 四种宝石碎片是**同一个斜棱柱**刷了 4 种颜色 ——
  数不透明像素是 89 / 89 / 89 / 89，分毫不差；
* 自然护符是绿宝石的换色，钛矿碎片是粗钛缩小一圈。

改完是**六种互不相同的轮廓**：

| 贴图 | 轮廓 | 想表达 |
|---|---|---|
| 钻石碎片 | **倒三角**（钻石的亭部） | 钻石被敲掉的那半个 |
| 绿宝石碎片 | **阶梯切面**（祖母绿切工） | 一圈一圈往里收的切工 |
| 青金石碎片 | **不规则碎块 + 斑点** | 青金石本来就是斑驳的石头 |
| 石英碎片 | **细长针状**（对角） | 石英是尖锐的半透明白晶 |
| 钛矿碎片 | **矿块 + 嵌着的金属斑** | 矿石 = 石头 + 里面的金属 |
| 自然护符 | **挂环 + 挂绳 + 一片叶子** | 挂在身上的护身符，不是一颗宝石 |

画法分三步：`bands()` 声明每行的左右端点（形状）→ `recolor*()` 只给**已有**
像素改明暗（配色，永远画不出界）→ `outline()` 自动描边（轮廓一定闭合）。

---
## 二、金属块（`textures/block/`）

铅块 / 钛块原来是一块**纯色板**：亮度只跨 32 / 23 档，加多少噪点都不像金属。
按 README §8.21 的结论，摆脱扁平感靠的是**结构而不是随机**：

1. **明暗结构** —— 外圈暗框、第二圈左上受光 / 右下背光、面内竖向渐变；
2. **金属噪点** —— 每像素 ±3.5% 亮度扰动（幅度收住，大了就成花岗岩）；
3. **立体细节** —— 铅是**压延横纹 + 四角铆钉**（哑光板材），
   钛是**一道 45° 镜面反射 + 抛光高光**（抛光件）。两者工艺不同，不能只是换个色。

铀块的花纹换到第三代：初版是一朵四瓣的浅色"花"，第二代是辐射三叶标志
（但三个扇叶在 12×12 上量化出来就是**三个三角**，读着像三角形拼图），
现在是**一块发光的六边形晶核** —— 没有三角，也更像"一块会发光的铀"。
配方还是那一条：深底 + 亮花纹，中间不描光晕。

配色沿用 README §8.26 定的色相：铅 = 冷调蓝灰、钛 = 微黄暖银（高光去色）。

---
用法：
    python _gen_textures.py                    # 写进 src/main/resources
    python _gen_textures.py --draft <目录>      # 写到别处（出图前先看预览）

跑一次会生成九张图，外加每个系列两张核对图（`_preview_*_textures*.png`）：
上排是**改动前**的旧图（取自 `build/` 里上次构建拷进去的那一份）、下排是新图。
"""

import os
import random
import sys

from PIL import Image

sys.stdout.reconfigure(encoding="utf-8", errors="replace")

SIZE = 16
ROOT = os.path.dirname(os.path.abspath(__file__))
TEX = os.path.join(ROOT, "src", "main", "resources", "assets", "moreores", "textures")
ITEM_DIR = os.path.join(TEX, "item")
BLOCK_DIR = os.path.join(TEX, "block")
# 对照图里的「旧图」：Gradle 构建会把贴图拷一份到 build/。
# 只要 build/ 里那份还是**改动前**的版本（也就是重绘之后没再构建过），
# 就拿它当「改动前」的那一排；已经重新构建过的话，上下两排会是同一批图。
BUILD_TEX = os.path.join(ROOT, "build", "resources", "main", "assets",
                         "moreores", "textures")


# ---------------------------------------------------------------- 画布

class Sprite:
    """一张 16×16 的字符画布：每个格子存一个字符，'.' 表示透明。"""

    def __init__(self):
        self.g = {}

    def put(self, x, y, ch):
        if 0 <= x < SIZE and 0 <= y < SIZE:
            self.g[(x, y)] = ch

    def at(self, x, y):
        return self.g.get((x, y))

    def fill(self, ch):
        for y in range(SIZE):
            for x in range(SIZE):
                self.g[(x, y)] = ch

    def ring(self, inset, ch):
        """往里缩 inset 圈画一个方框（inset=0 就是最外圈）。"""
        lo, hi = inset, SIZE - 1 - inset
        for i in range(lo, hi + 1):
            for p in ((i, lo), (i, hi), (lo, i), (hi, i)):
                self.put(*p, ch)

    def bands(self, table, ch):
        """填色（会创建新像素）。table: {y: (x0, x1)}"""
        for y, (x0, x1) in table.items():
            for x in range(x0, x1 + 1):
                self.put(x, y, ch)

    def pixels(self, pts, ch):
        for x, y in pts:
            self.put(x, y, ch)

    def recolor(self, pts, ch):
        """只改已有像素的颜色，不会画出界。"""
        for x, y in pts:
            if (x, y) in self.g:
                self.g[(x, y)] = ch

    def recolor_bands(self, table, ch):
        for y, (x0, x1) in table.items():
            for x in range(x0, x1 + 1):
                if (x, y) in self.g:
                    self.g[(x, y)] = ch

    def outline(self, ch):
        """给所有贴着实心像素的透明格子补上轮廓色（4 邻域）。"""
        add = {}
        for y in range(SIZE):
            for x in range(SIZE):
                if (x, y) in self.g:
                    continue
                for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                    if (x + dx, y + dy) in self.g:
                        add[(x, y)] = ch
                        break
        self.g.update(add)

    def image(self, palette):
        im = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))
        px = im.load()
        for (x, y), ch in self.g.items():
            if ch == ".":
                continue
            r, g, b = palette[ch]
            px[x, y] = (r, g, b, 255)
        return im


def octagon(x0, x1, y0, y1, cut):
    """切掉四角的矩形（45° 倒角），返回 {y: (x0, x1)}。

    配合「x±1 / y±1 / cut-1」就是往里缩一圈的同心八边形 ——
    阶梯切面全靠这个套娃。
    """
    out = {}
    for y in range(y0, y1 + 1):
        inset = max(0, cut - min(y - y0, y1 - y))
        out[y] = (x0 + inset, x1 - inset)
    return out


def metal_noise(im, amount, seed, quant=4):
    """金属噪点：每像素亮度 ±amount 的扰动，再量化到 quant 的整数倍。

    幅度必须收住 —— README §8.21 记过，±9% 出来是**花岗岩**不是金属；
    量化是为了别让 256 个像素各自变成一个颜色，噪点要"看得见但不脏"。
    """
    rnd = random.Random(seed)
    px = im.load()
    for y in range(SIZE):
        for x in range(SIZE):
            r, g, b, a = px[x, y]
            k = 1.0 + rnd.uniform(-amount, amount)
            px[x, y] = (min(255, max(0, int(round(r * k / quant)) * quant)),
                        min(255, max(0, int(round(g * k / quant)) * quant)),
                        min(255, max(0, int(round(b * k / quant)) * quant)), a)
    return im


# ================================================================ 一、物品
#
# 每张图 5 档明度：轮廓 a / 暗 b / 中 c / 亮 d / 高光 e。
# 色相取对应宝石自己的，但**明暗结构是各自设计的**，不共用一套轮廓。

ITEM_PALETTES = {
    "diamond_shard": {
        "a": (18, 56, 68), "b": (32, 100, 116), "c": (66, 168, 182),
        "d": (122, 228, 236), "e": (204, 255, 255),
    },
    "emerald_shard": {
        "a": (14, 62, 36), "b": (26, 110, 62), "c": (46, 176, 92),
        "d": (94, 234, 130), "e": (178, 255, 196),
    },
    "lapis_shard": {
        "a": (16, 28, 84), "b": (36, 62, 152), "c": (56, 94, 204),
        "d": (94, 146, 238), "e": (170, 208, 255),
    },
    "quartz_shard": {
        "a": (86, 78, 70), "b": (138, 128, 118), "c": (196, 186, 174),
        "d": (232, 224, 212), "e": (253, 250, 244),
    },
    "titanium_ore_fragment": {
        "a": (40, 44, 52), "b": (70, 76, 86), "c": (100, 108, 120),
        "d": (132, 142, 154), "e": (196, 208, 224), "f": (240, 248, 255),
    },
    "nature_talisman": {
        "a": (22, 34, 20), "b": (22, 84, 46), "c": (48, 148, 76),
        "d": (102, 208, 118), "e": (186, 250, 176),
        "u": (58, 38, 22), "v": (108, 74, 42),
    },
}


# ---------------------------------------------------------------- 钻石碎片
# 轮廓：钻石的**亭部**（倒三角），底尖被敲掉一截。
# 明暗只分三块：腰棱高光、左边受光面、右边背光面 —— 面越少越像切割过的宝石。

def diamond_shard():
    s = Sprite()
    body = {2: (2, 13), 3: (3, 12), 4: (3, 12), 5: (4, 11), 6: (4, 11),
            7: (5, 10), 8: (5, 10), 9: (6, 9), 10: (6, 9)}
    s.bands(body, "c")                                  # 右面：中间调
    s.recolor_bands({3: (3, 8), 4: (3, 7), 5: (4, 6), 6: (4, 5),
                     7: (5, 6), 8: (5, 6), 9: (6, 6)}, "d")   # 左面：受光
    s.recolor_bands({2: (2, 13)}, "e")                  # 腰棱：整条顶边高光
    for y in range(4, 11):                              # 右面外缘压暗，做出厚度
        s.recolor([(body[y][1], y)], "b")
    s.pixels([(6, 11)], "d")                            # 亭尖裂成两个小尖
    s.pixels([(9, 11)], "b")
    s.outline("a")
    return s


# ---------------------------------------------------------------- 绿宝石碎片
# 轮廓：**祖母绿切工** —— 外圈 / 台阶 / 台面，三级同心八边形。
# 每级比外一级缩进恰好 1 像素（x±1、y±1、倒角-1），台阶才均匀。
# 左下角崩掉一块，说明它是碎片而不是一颗完整的宝石。

def emerald_shard():
    s = Sprite()
    lv = [octagon(3, 12, 3, 12, 2), octagon(4, 11, 4, 11, 1),
          octagon(5, 10, 5, 10, 0)]
    for table, ch in zip(lv, ("c", "d", "e")):
        s.bands(table, ch)

    def ring(a, b):
        """a 里挖掉 b 剩下的那一圈。"""
        return [(x, y) for y, (x0, x1) in a.items() for x in range(x0, x1 + 1)
                if not (y in b and b[y][0] <= x <= b[y][1])]

    # 斜着分光：左上角亮、右下角暗。
    # 台阶要「看得出来」靠的就是这条 45° 分界 —— 角上是斜的、正上和正下是横的，
    # 阶梯切面才有棱有角，不然就是一块糊掉的绿疙瘩。
    for i, (light, mid, dark) in enumerate((("d", "c", "b"), ("e", "d", "c"))):
        for x, y in ring(lv[i], lv[i + 1]):
            k = x + y
            s.recolor([(x, y)], light if k <= 13 else dark if k >= 19 else mid)
    for y, (x0, x1) in lv[2].items():
        for x in range(x0, x1 + 1):
            s.recolor([(x, y)], "d" if x + y >= 19 else "e")
    for p in [(3, 10), (4, 11), (5, 12)]:       # 左下角崩掉的一块
        s.g.pop(p, None)
    s.outline("a")
    return s


# ---------------------------------------------------------------- 青金石碎片
# 轮廓：**不规则碎块 + 斑点**。青金石在原版里就是一块斑驳的深蓝石头，
# 所以这张不追求切面，追求「碎」和「花」。

def lapis_shard():
    s = Sprite()
    body = {4: (6, 10), 5: (4, 11), 6: (3, 12), 7: (2, 13), 8: (2, 13),
            9: (3, 12), 10: (3, 12), 11: (4, 11), 12: (6, 10), 13: (7, 9)}
    s.bands(body, "c")
    s.recolor_bands({4: (6, 10), 5: (4, 8), 6: (3, 6), 7: (2, 5), 8: (2, 4)}, "d")
    s.recolor_bands({9: (10, 12), 10: (9, 12), 11: (8, 11),
                     12: (8, 10), 13: (8, 9)}, "b")
    # 亮斑：深蓝底上撒浅蓝点，这是青金石最好认的特征
    s.recolor([(5, 6), (9, 5), (6, 9), (11, 8), (8, 11), (10, 10), (4, 8)], "e")
    # 暗斑：和亮斑错开，不然会糊成一片
    s.recolor([(7, 7), (4, 10), (11, 6), (9, 12), (6, 12), (12, 9)], "a")
    s.outline("a")
    return s


# ---------------------------------------------------------------- 石英碎片
# 轮廓：**细长针状**，沿对角线摆。石英是尖锐的半透明白晶，
# 靠「左上缘高光 + 右下缘背光」两条边把体积撑起来；
# 背光侧在中段故意断两处，边缘有豁口才像敲下来的碎片。

def quartz_shard():
    s = Sprite()
    body = {2: (12, 13), 3: (11, 13), 4: (10, 12), 5: (10, 12), 6: (9, 11),
            7: (8, 11), 8: (7, 10), 9: (6, 9), 10: (5, 8), 11: (4, 7),
            12: (3, 5), 13: (3, 4)}
    s.bands(body, "d")
    for y, (x0, x1) in body.items():
        s.recolor([(x0, y)], "e")
        if x1 - x0 >= 2:
            s.recolor([(x1 - 1, y)], "c")
        s.recolor([(x1, y)], "d" if y in (5, 9) else "b")
    s.outline("a")
    return s


# ---------------------------------------------------------------- 钛矿碎片
# 轮廓：**矿块 + 嵌在里面的金属斑**。
# 原来是把粗钛缩小 78%，看不出「矿」；矿石的关键是「石头里裹着金属」，
# 所以石头要够灰、金属斑要够小够尖 —— 大块抹白就成了石头上的雪。

def titanium_ore_fragment():
    s = Sprite()
    body = {3: (7, 10), 4: (5, 11), 5: (4, 12), 6: (3, 11), 7: (3, 12),
            8: (3, 12), 9: (4, 12), 10: (4, 11), 11: (4, 11), 12: (5, 10),
            13: (7, 9)}
    s.bands(body, "c")
    s.recolor_bands({3: body[3], 4: (5, 9), 5: (4, 6), 6: (3, 5)}, "d")   # 左上受光
    s.recolor([(3, 7), (3, 8), (4, 9), (4, 10)], "d")
    s.recolor_bands({9: (10, 12), 10: (9, 11), 11: (9, 11),
                     12: (8, 10), 13: (8, 9)}, "b")                       # 右下背光
    # 三簇钛：亮块带尖角，受光侧包一圈次级亮色
    s.recolor([(5, 6), (6, 6), (5, 7)], "f")
    s.recolor([(4, 6), (5, 5), (4, 7)], "e")
    s.recolor([(9, 8), (10, 8), (9, 9)], "f")
    s.recolor([(8, 8), (9, 7), (8, 9)], "e")
    s.recolor([(7, 11), (8, 11)], "f")
    s.recolor([(6, 11), (7, 10), (6, 10)], "e")
    s.outline("a")
    return s


# ---------------------------------------------------------------- 自然护符
# 轮廓：**挂环 + 挂绳 + 一片叶子**。
# 原来是绿宝石换色；护身符要能看出是「挂在身上的东西」，
# 而且形状本身就该说话 —— 所以吊坠直接做成叶片，而不是圆牌上再刻一片小叶子
#（16×16 上，牌面里的小图案只会糊成一个点）。

def nature_talisman():
    s = Sprite()
    # 挂环：5×4 的小圆环，中间留孔。
    # 整体从 y=1 起、到 y=14 止 —— 上下各留一格给自动描边，
    # 不然环顶贴在画布上沿、描不出边，看着像被裁掉了。
    s.pixels([(7, 1), (8, 1), (9, 1), (6, 2), (10, 2),
              (6, 3), (10, 3), (7, 4), (8, 4), (9, 4)], "v")
    s.recolor([(7, 4), (8, 4), (9, 4)], "u")
    s.pixels([(8, 5)], "u")                       # 挂绳
    # 叶片：肩在上（接挂绳），顶尖朝下
    leaf = {6: (6, 10), 7: (5, 11), 8: (5, 11), 9: (4, 12), 10: (4, 12),
            11: (5, 12), 12: (5, 11), 13: (6, 10), 14: (7, 9)}
    s.bands(leaf, "d")
    for y, (x0, x1) in leaf.items():
        s.recolor([(x1, y)], "c")                 # 背光的那条边
    s.recolor([(8, y) for y in range(6, 15)], "e")   # 主叶脉
    s.recolor([(6, 8), (10, 10), (7, 12)], "c")      # 三条侧脉
    s.outline("a")
    return s


ITEM_BUILDERS = [
    ("diamond_shard", diamond_shard),
    ("emerald_shard", emerald_shard),
    ("lapis_shard", lapis_shard),
    ("quartz_shard", quartz_shard),
    ("titanium_ore_fragment", titanium_ore_fragment),
    ("nature_talisman", nature_talisman),
]


# ================================================================ 二、金属块
#
# 每张 8 档：a 外框 / b 背光边 / g 面(暗) / c 面(中) / d 面(亮) / e 受光边 /
# f 高光 / 另加 h 只给铀的发光标志。
#
# 色相按 README §8.26：铅 = 冷调蓝灰 (0.90, 0.98, 1.13)、
# 钛 = 微黄暖银 (1.04, 1.01, 0.95)，且**钛的高光要去色**（坑③：整体加暖会变奶油色）。

BLOCK_PALETTES = {
    "lead_block": {
        "a": (44, 52, 64), "b": (70, 82, 98), "g": (92, 106, 126),
        "c": (114, 130, 152), "d": (142, 160, 184), "e": (176, 194, 216),
        "f": (214, 228, 246),
    },
    "titanium_block": {
        "a": (70, 69, 64), "b": (108, 107, 100), "g": (146, 143, 134),
        "c": (180, 176, 166), "d": (210, 207, 197), "e": (236, 233, 226),
        "f": (253, 251, 246),
    },
    "uranium_block": {
        "a": (24, 40, 28), "b": (40, 64, 42), "g": (58, 92, 58),
        "c": (74, 114, 70), "d": (96, 142, 86), "e": (128, 186, 108),
        "f": (176, 236, 146), "h": (228, 255, 200),
    },
}


def metal_plate():
    """三张金属块的通用骨架：外圈暗框 → 第二圈左上受光 / 右下背光 → 面。

    摆脱扁平感靠的是**结构**（README §8.21），不是随机噪点：
    原来的铅块 / 钛块亮度只跨 32 / 23 档，几乎是一块纯色板，
    所以怎么加噪点都不像金属。这里是先把明暗骨架搭出来。
    """
    s = Sprite()
    s.fill("c")
    s.ring(0, "a")
    for i in range(1, 15):
        s.put(i, 1, "e")      # 上边受光
        s.put(1, i, "e")      # 左边受光
        s.put(i, 14, "b")     # 下边背光
        s.put(14, i, "b")     # 右边背光
    s.put(1, 1, "e")          # 左上角最亮
    s.put(14, 1, "d")         # 右上角过渡
    s.put(1, 14, "b")
    s.put(14, 14, "a")        # 右下角最暗
    # 面内竖向渐变：一块板子被上方的光照亮
    s.recolor_bands({2: (2, 13), 3: (2, 13), 4: (2, 13)}, "d")
    s.recolor_bands({11: (2, 13), 12: (2, 13), 13: (2, 13)}, "g")
    return s


# ---------------------------------------------------------------- 铅块
# 铅是**压延**出来的板子：横向轧制痕 + 四角铆钉，哑光、色阶偏冷。

def lead_block():
    s = metal_plate()
    s.recolor_bands({6: (3, 12)}, "g")            # 轧制暗纹
    s.recolor_bands({8: (2, 12)}, "d")            # 轧制亮纹
    s.recolor([(5, 3), (6, 3), (7, 3)], "e")      # 面上方一道窄高光
    # 四角铆钉：2×2，左上受光、右下背光 —— 一颗"凸出来"的钉子
    # （高光只用 e 不用 f：f 太亮，四颗钉子在暗色板面上会像四盏灯）
    for x, y in ((3, 3), (11, 3), (3, 11), (11, 11)):
        s.put(x, y, "e")
        s.put(x + 1, y, "d")
        s.put(x, y + 1, "d")
        s.put(x + 1, y + 1, "b")
    return s


# ---------------------------------------------------------------- 钛块
# 钛是**抛光**件：整块板做镜面反射式的竖向渐变（上亮下暗，但分段更明确），
# 左上角一团抛光高光。和铅的横纹 + 铆钉形成对比 —— 同样是金属板，工艺不一样。
#
# ⚠️ 第一版在面上铺了「每 3 像素一道 45° 拉丝」，结果 16×16 下密到看不出方向，
# 读出来是**花岗岩 / 粗花呢**（正是 README §8.21 警告过的那个坑）。
# 抛光面不需要纹理，需要的是**干净的大色阶**。

def titanium_block():
    s = metal_plate()
    # 把明暗压成三段：上段反射天光、中段、下段背光
    s.recolor_bands({5: (2, 13), 6: (2, 13)}, "d")
    s.recolor_bands({9: (2, 13), 10: (2, 13)}, "g")
    # 一道斜向镜面反射 —— 抛光面最认这个：x+y 相同就是一条 45° 的斜线，
    # 取 10/11 那两条正好从板面左下扫到右上。只在面上动，别碰到边框。
    for y in range(2, 14):
        for x in range(2, 14):
            if s.at(x, y) not in ("c", "d", "g"):
                continue
            if x + y in (10, 11):
                s.recolor([(x, y)], "e")
            elif x + y == 13:
                s.recolor([(x, y)], "c")
    # 左上角一团抛光高光（高光去色，见 README §8.26 坑③）
    s.recolor([(2, 2), (3, 2), (2, 3)], "f")
    s.recolor([(4, 2), (2, 4)], "e")
    return s


# ---------------------------------------------------------------- 铀块
# 深色金属板 + 中心一块**发光的六边形晶核**。
#
# 花纹换过三代：
#   ① 四瓣浅色"花朵"（初版）—— 既不像金属也不像放射源；
#   ② 辐射三叶标志 —— 但三个扇叶在 12×12 上量化出来就是**三个三角**，
#      读着像三角形拼图，不像标志（用户也这么反馈）；
#   ③ 现在这块六边形晶体：没有三角，也更像"一块会发光的铀"。
#
# 通用的一条：**对比全靠深底**。板子压到最暗两档，晶体提到接近纯白，
# 中间不要描光晕 —— 一圈光晕就会把晶体和板面之间的界限糊掉。

# 六边形晶核：10 行，最宽 8 像素。**直接按像素摆**，
# 用「半径 ± 张角」算出来的形状在 12×12 上会被量化毁掉（见上面第 ② 代）。
CRYSTAL = [
    ".....##.....",
    "....####....",
    "...######...",
    "..########..",
    "..########..",
    "..########..",
    "..########..",
    "...######...",
    "....####....",
    ".....##.....",
]


def uranium_block():
    s = metal_plate()
    # 面整体压到最暗两档 —— 对比度全留给晶体
    s.recolor_bands({y: (2, 13) for y in range(2, 14)}, "g")
    s.recolor_bands({2: (2, 13), 3: (2, 13), 4: (2, 13),
                     5: (2, 13), 6: (2, 13)}, "c")
    s.recolor_bands({12: (2, 13), 13: (2, 13)}, "b")

    body = {(2 + c, 3 + r) for r, row in enumerate(CRYSTAL)
            for c, ch in enumerate(row) if ch == "#"}
    # 晶体的最外一圈单独提亮，晶体才从深底上"立"起来
    edge = {(x, y) for (x, y) in body
            if any((x + dx, y + dy) not in body
                   for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)))}
    inner = body - edge
    # 内部按 45° 分光：左上受光、右下背光。
    # 受光面直接用最亮的一档（和边缘同色）—— 晶体才有"切面"，
    # 不然整个六边形是一块平色，只剩中间一个亮点。
    s.recolor([p for p in inner if sum(p) <= 13], "f")
    s.recolor([p for p in inner if sum(p) > 13], "d")
    s.recolor(list(edge), "f")
    # 中心的白热核：一枚 **2-4-2 的小菱形**。
    # 铺成等宽竖条会读成"药丸"，铺到 16 像素会读成"绿眼睛"——
    # 这个大小刚好是"里头有东西在发亮"。
    core = [(7, 6), (8, 6), (6, 7), (7, 7), (8, 7), (9, 7), (7, 8), (8, 8)]
    s.recolor([p for p in core if p in body], "h")
    return s


BLOCK_BUILDERS = [
    ("lead_block", lead_block, 3.5, 0.035, 1101),
    ("titanium_block", titanium_block, 3.5, 0.035, 2202),
    ("uranium_block", uranium_block, 2.5, 0.030, 3303),
]


# ================================================================ 主流程

def render(builders, palettes, out_dir, before_dir, preview, zoom, quant=4):
    os.makedirs(out_dir, exist_ok=True)
    images = []
    for entry in builders:
        name, build = entry[0], entry[1]
        noise = entry[3] if len(entry) > 3 else 0.0
        seed = entry[4] if len(entry) > 4 else 0
        im = build().image(palettes[name])
        if noise:
            im = metal_noise(im, noise, seed, quant)
        im.save(os.path.join(out_dir, name + ".png"))
        cols = len(im.getcolors(maxcolors=SIZE * SIZE) or [])
        images.append((name, im))
        print("生成 %-24s 16x16  颜色数=%3d" % (name + ".png", cols))

    # 核对图：上排 = 改动前（build/ 里那份），下排 = 刚生成的
    scale, cols = 12, min(3, len(images))
    cell = SIZE * scale
    rows = (len(images) + cols - 1) // cols
    sheet = Image.new("RGBA", (cell * cols, cell * rows * 2), (64, 64, 70, 255))
    for i, (name, im) in enumerate(images):
        cx, cy = (i % cols) * cell, (i // cols) * cell * 2
        old = os.path.join(before_dir, name + ".png")
        if not os.path.exists(old):
            old = os.path.join(out_dir, name + ".png")
        if os.path.exists(old):
            sheet.alpha_composite(Image.open(old).convert("RGBA")
                                  .resize((cell, cell), Image.NEAREST), (cx, cy))
        sheet.alpha_composite(im.resize((cell, cell), Image.NEAREST),
                              (cx, cy + cell))
    sheet.save(preview)
    print("预览图 ->", preview)

    # 放大图：只看新图，逐个像素核对
    zoom_px = 20
    zc = SIZE * zoom_px
    zoomed = Image.new("RGBA", (zc * cols, zc * rows), (72, 72, 78, 255))
    for i, (_name, im) in enumerate(images):
        zoomed.alpha_composite(im.resize((zc, zc), Image.NEAREST),
                               ((i % cols) * zc, (i // cols) * zc))
    zoomed.save(zoom)
    print("放大图 ->", zoom)


def ascii_map(builders, palettes):
    """把每张图按亮度打成 ASCII —— 16×16 上「结构对不对」看字符比看图准。

    亮度按**本图的最暗 / 最亮**归一化，这样每张图的层次都能看清；
    绝对亮度看数字（`render` 会打印颜色数，另有用小工具量均值）。
    """
    ramp = " .:-=+*#%@"
    for entry in builders:
        name, build = entry[0], entry[1]
        pal = palettes[name]
        s = build()
        lum = {}
        for (x, y), ch in s.g.items():
            r, g, b = pal[ch]
            lum[(x, y)] = 0.299 * r + 0.587 * g + 0.114 * b
        lo, hi = min(lum.values()), max(lum.values())
        print("### %s  亮度 %.0f~%.0f" % (name, lo, hi))
        for y in range(SIZE):
            row = ""
            for x in range(SIZE):
                v = lum.get((x, y))
                row += "?" if v is None else ramp[int((v - lo) / (hi - lo + 1e-9) * 9)]
            print("    " + row)
        print()


def main():
    if len(sys.argv) >= 2 and sys.argv[1] == "--ascii":
        ascii_map(ITEM_BUILDERS, ITEM_PALETTES)
        ascii_map(BLOCK_BUILDERS, BLOCK_PALETTES)
        return

    draft = None
    if len(sys.argv) >= 3 and sys.argv[1] == "--draft":
        draft = sys.argv[2]
    item_out = os.path.join(draft, "item") if draft else ITEM_DIR
    block_out = os.path.join(draft, "block") if draft else BLOCK_DIR

    print("== 物品 ==")
    render(ITEM_BUILDERS, ITEM_PALETTES, item_out,
           os.path.join(BUILD_TEX, "item"),
           os.path.join(ROOT, "_preview_item_textures.png"),
           os.path.join(ROOT, "_preview_item_textures_zoom.png"))
    print("== 金属块 ==")
    render(BLOCK_BUILDERS, BLOCK_PALETTES, block_out,
           os.path.join(BUILD_TEX, "block"),
           os.path.join(ROOT, "_preview_block_textures.png"),
           os.path.join(ROOT, "_preview_block_textures_zoom.png"))


if __name__ == "__main__":
    main()
