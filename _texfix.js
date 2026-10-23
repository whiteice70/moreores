(() => {
  const out = {};
  const t = Texture.all[0];
  const map = t.material && t.material.map;

  out.before = map && map.image
    ? (map.image.width + 'x' + map.image.height) : null;

  // 关键：t.img（我 load 进去的 256x256）和 material.map.image（Blockbench
  // 早先 loadEmpty() 建出来的 16x16 画布）是**两个对象**。
  // 只设 source 不会替换 material 里那张，得手动换掉再打脏标。
  if (map && t.img) {
    map.image = t.img;
    map.needsUpdate = true;
    t.material.needsUpdate = true;
  }

  out.after = map && map.image
    ? (map.image.width + 'x' + map.image.height) : null;
  out.needsUpdate = map ? map.needsUpdate : null;

  Canvas.updateAll();
  return JSON.stringify(out, null, 2);
})()
