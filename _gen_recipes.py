# -*- coding: utf-8 -*-
"""临时脚本：把模组所有配方读出来，生成「附录 A：全部配方清单」。

**为什么要脚本生成而不是手写** —— 模组已经有一百多条配方，手抄必然漏，
而且配方一改文档就过期。这个脚本直接读 data/moreores/recipe/ 下的 JSON，
跑一次就是当前的真值。

用法：python _gen_recipes.py   （会把附录写进 物品与合成表.md）
"""

import io
import json
import os
import sys

sys.stdout.reconfigure(encoding="utf-8", errors="replace")

ROOT = r"E:\Study\PythonTest\my_first_mod\src\main\resources"
RECIPE_DIR = os.path.join(ROOT, "data", "moreores", "recipe")
LANG = os.path.join(ROOT, "assets", "moreores", "lang", "zh_cn.json")
DOC = r"E:\Study\PythonTest\my_first_mod\物品与合成表.md"

BEGIN = "<!-- RECIPES:BEGIN -->"
END = "<!-- RECIPES:END -->"

with io.open(LANG, encoding="utf-8") as f:
    LANG_ZH = json.load(f)

# 模组的 lang 里**只有自己的条目**，原版物品的译名得自己补一份
# （否则附录里会看到 minecraft:wheat_seeds 这种原始 id）
VANILLA_ZH = {
    "glass": "玻璃", "glass_pane": "玻璃板", "glass_bottle": "玻璃瓶",
    "dirt": "泥土", "farmland": "耕地",
    "wheat_seeds": "小麦种子", "wheat": "小麦",
    "coal": "煤炭", "charcoal": "木炭",
    "copper_ingot": "铜锭", "iron_ingot": "铁锭", "gold_ingot": "金锭",
    "netherite_ingot": "下界合金锭", "netherite_scrap": "下界合金碎片",
    "iron_nugget": "铁粒", "gold_nugget": "金粒", "copper_nugget": "铜粒",
    "diamond": "钻石", "emerald": "绿宝石", "lapis_lazuli": "青金石",
    "quartz": "石英", "redstone": "红石",
    "furnace": "熔炉", "blast_furnace": "高炉", "smoker": "烟熏炉",
    "campfire": "营火", "crafting_table": "工作台",
    "stick": "木棍", "feather": "羽毛", "string": "线",
    "milk_bucket": "牛奶桶", "bucket": "桶", "book": "书",
    "rotten_flesh": "腐肉", "bone_meal": "骨粉", "leather": "皮革",
    "beef": "生牛肉", "porkchop": "生猪排", "chicken": "生鸡肉",
    "mutton": "生羊肉", "rabbit": "生兔肉",
    "cod": "生鳕鱼", "salmon": "生鲑鱼", "tropical_fish": "热带鱼",
    "pufferfish": "河豚",
    "glow_berries": "发光浆果", "sweet_berries": "甜浆果",
    "oak_sapling": "橡树树苗", "spruce_sapling": "云杉树苗",
    "birch_sapling": "白桦树苗", "jungle_sapling": "丛林树苗",
    "acacia_sapling": "金合欢树苗", "dark_oak_sapling": "深色橡树树苗",
    "cherry_sapling": "樱花树苗", "mangrove_sapling": "红树树苗",
    "pale_oak_sapling": "苍白橡树树苗",
    "oak_log": "橡木原木", "spruce_log": "云杉原木", "birch_log": "白桦原木",
    "jungle_log": "丛林原木", "acacia_log": "金合欢原木",
    "dark_oak_log": "深色橡树原木", "cherry_log": "樱花原木",
    "mangrove_log": "红树原木", "pale_oak_log": "苍白橡树原木",
    "mangrove_propagule": "红树胎生苗",
    "carrot": "胡萝卜", "potato": "马铃薯", "beetroot": "甜菜根",
    "melon": "西瓜", "melon_slice": "西瓜片", "pumpkin": "南瓜",
    "cocoa_beans": "可可豆", "nether_wart": "下界疣",
    "melon_seeds": "西瓜种子", "pumpkin_seeds": "南瓜种子",
    "beetroot_seeds": "甜菜种子", "torchflower_seeds": "火炬花种子",
    "pitcher_pod": "瓶子草豆荚",
    "white_wool": "白色羊毛", "orange_wool": "橙色羊毛",
    "magenta_wool": "品红色羊毛", "light_blue_wool": "淡蓝色羊毛",
    "yellow_wool": "黄色羊毛", "lime_wool": "黄绿色羊毛",
    "pink_wool": "粉红色羊毛", "gray_wool": "灰色羊毛",
    "light_gray_wool": "淡灰色羊毛", "cyan_wool": "青色羊毛",
    "purple_wool": "紫色羊毛", "blue_wool": "蓝色羊毛",
    "brown_wool": "棕色羊毛", "green_wool": "绿色羊毛",
    "red_wool": "红色羊毛", "black_wool": "黑色羊毛",
    "sulfur": "硫磺", "gunpowder": "火药",
}

# 配方里用到的标签：展开成"里面实际是什么"，比裸 id 好读
TAG_ZH = {
    "#minecraft:dirt": "泥土（草方块 / 灰化土 / 菌丝体 / 泥巴都行）",
    "#moreores:lead_tool_materials": "铅锭",
    "#moreores:uranium_tool_materials": "铀锭",
    "#moreores:titanium_tool_materials": "钛锭",
    "#moreores:lead_armor_repair": "铅锭",
    "#moreores:titanium_armor_repair": "钛锭",
}


def name_of(item_id):
    """物品 id / 标签 → 中文名，取不到就退化成 id。"""
    if item_id.startswith("#"):
        return TAG_ZH.get(item_id, item_id)

    if item_id.startswith("minecraft:"):
        bare = item_id.split(":", 1)[1]
        return VANILLA_ZH.get(bare, bare)

    key = "item." + item_id.replace(":", ".")

    return LANG_ZH.get(key, LANG_ZH.get("block." + item_id.replace(":", "."),
                                        item_id))


def fmt_item(entry):
    """一个 ingredient / result 条目 → '中文名 ×n'。"""
    if isinstance(entry, str):
        return name_of(entry)
    if isinstance(entry, dict):
        # 26.3 的内联列表：{"item": ..., "count": n}
        item = entry.get("item", entry.get("id", "?"))
        count = entry.get("count", 1)
        return name_of(item) + (f" ×{count}" if count > 1 else "")

    return str(entry)


def fmt_ingredients(recipe):
    """材料可能是字符串 / 标签 / 列表 / 内联 dict。

    ⚠️ **无序配方的多个 ingredient 是「全部都要放」，要连 `+`；
    而某个 ingredient 内部的列表才是「任选其一」，要连「或」** ——
    两者不能混。硫磺火药那条就是例子：硫磺粉 + 煤/木炭 + 骨粉。
    """
    ing = recipe.get("ingredient", recipe.get("ingredients", []))

    if isinstance(ing, str):
        return name_of(ing.lstrip("#"))

    if isinstance(ing, list):
        # 顶层列表 = 都要放；里面的内联列表 = 任选
        parts = []
        for entry in ing:
            if isinstance(entry, str):
                parts.append(name_of(entry.lstrip("#")))
            elif isinstance(entry, dict):
                parts.append(fmt_item(entry))
            elif isinstance(entry, list):
                parts.append(" / ".join(name_of(e.lstrip("#")) for e in entry))
        return " + ".join(parts)

    return fmt_item(ing)


def fmt_shaped(recipe):
    """有序配方 → 图案 + 图例。"""
    key = recipe.get("key", {})
    pattern = recipe.get("pattern", [])
    legend = "  ".join(f"{k}={name_of(v)}" for k, v in key.items())
    return " / ".join(pattern), legend


def fmt_result(recipe):
    result = recipe.get("result", {})
    if isinstance(result, str):
        return name_of(result)
    count = result.get("count", 1)
    return name_of(result.get("id", "?")) + (f" ×{count}" if count > 1 else "")


# ---------------- 收集 ----------------
entries = []

for base, dirs, files in os.walk(RECIPE_DIR):
    for name in sorted(files):
        if not name.endswith(".json"):
            continue

        rel = os.path.relpath(os.path.join(base, name), RECIPE_DIR)
        path = os.path.join(base, name)

        with io.open(path, encoding="utf-8") as f:
            data = json.load(f)

        rtype = data.get("type", "?")
        rtype = rtype.replace("minecraft:", "").replace("moreores:", "")
        entries.append((rel, rtype, data))

entries.sort(key=lambda e: (e[1], e[0]))

# 按类型分组
groups = {}
for rel, rtype, data in entries:
    groups.setdefault(rtype, []).append((rel, data))

TITLES = {
    "crafting_shaped": "有序合成",
    "crafting_shapeless": "无序合成",
    "smelting": "熔炉冶炼",
    "blasting": "高炉冶炼",
    "high_energy_smelting": "高能熔炉冶炼（模组专有）",
    "bio_catalyzing": "生物催化（模组专有）",
    "mineral_catalyzing": "矿物催化（模组专有）",
}

lines = [f"### 全部配方（共 {len(entries)} 条）\n"]
lines.append("> 本节由脚本直接读取 `data/moreores/recipe/` 生成，"
             "是当前数据包的真值 —— 改配方后重跑 `_gen_recipes.py` 即可。\n")

for rtype in ["crafting_shaped", "crafting_shapeless", "smelting", "blasting",
              "high_energy_smelting", "bio_catalyzing", "mineral_catalyzing"]:
    items = groups.get(rtype)

    if not items:
        continue

    lines.append(f"\n#### {TITLES[rtype]}（{len(items)} 条）\n")

    if rtype == "crafting_shaped":
        lines.append("| 配方文件 | 图案 | 材料 | 产物 |")
        lines.append("|---|---|---|---|")

        for rel, data in items:
            pattern, legend = fmt_shaped(data)
            lines.append(f"| `{rel[:-5]}` | `{pattern}` | {legend} | {fmt_result(data)} |")

    elif rtype == "crafting_shapeless":
        lines.append("| 配方文件 | 材料 | 产物 |")
        lines.append("|---|---|---|")

        for rel, data in items:
            rel = rel.replace("\\", "/")[:-5]
            lines.append(f"| `{rel}` | {fmt_ingredients(data)} | {fmt_result(data)} |")

    else:
        lines.append("| 配方文件 | 材料 | 产物 |")
        lines.append("|---|---|---|")

        for rel, data in items:
            rel = rel.replace("\\", "/")[:-5]
            lines.append(f"| `{rel}` | {fmt_ingredients(data)} | {fmt_result(data)} |")

# ---------------- 插进文档 ----------------
block = "\n".join(lines)

with io.open(DOC, encoding="utf-8") as f:
    doc = f.read()

if BEGIN in doc:
    head, _, rest = doc.partition(BEGIN)
    _, _, tail = rest.partition(END)
    doc = head + BEGIN + "\n" + block + "\n" + END + tail
else:
    doc = doc.rstrip() + "\n\n---\n\n## 附录 A：配方全清单\n\n" + BEGIN + "\n" + block + "\n" + END + "\n"

with io.open(DOC, "w", encoding="utf-8", newline="\n") as f:
    f.write(doc)

print(f"共 {len(entries)} 条配方，分组：")
for k, v in groups.items():
    print(f"  {TITLES.get(k, k):28s} {len(v):3d} 条")
print("\n已写入 物品与合成表.md 的附录 A")
