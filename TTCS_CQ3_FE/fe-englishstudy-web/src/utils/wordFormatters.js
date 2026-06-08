export function formatWordType(type) {
  if (!type || typeof type !== 'string') return 'Khác';

  const t = type.trim().toLowerCase();

  // Các biến thể của Danh từ
  if (['n', 'noun', 'danh từ', 'danh tu', 'danh'].includes(t)) return 'Danh từ';

  // Các biến thể của Động từ
  if (['v', 'verb', 'động từ', 'dong tu', 'động'].includes(t)) return 'Động từ';

  // Các biến thể của Tính từ
  if (['adj', 'adjective', 'tính từ', 'tinh tu', 'tính'].includes(t)) return 'Tính từ';

  // Các biến thể của Trạng từ
  if (['adv', 'adverb', 'trạng từ', 'trang tu', 'trạng'].includes(t)) return 'Trạng từ';

  // Trả về dạng capitalize chữ cái đầu nếu không khớp pattern trên
  return type.charAt(0).toUpperCase() + type.slice(1).toLowerCase();
}
