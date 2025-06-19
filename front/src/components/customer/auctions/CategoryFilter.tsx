interface CategoryFilterProps {
  category: string;
  onChange: (newCategory: string) => void;
  disabled: boolean;
}

export default function CategoryFilter({ category, onChange, disabled }: CategoryFilterProps) {
  const categories = ['', '가구', '전자제품', '의류'];

  return (
    <div style={{ marginBottom: 20 }}>
      <label htmlFor="category-select" style={{ marginRight: 10 }}>카테고리:</label>
      <select
        id="category-select"
        value={category}
        onChange={(e) => onChange(e.target.value)}
        disabled={disabled}
      >
        {categories.map((c) => (
          <option key={c} value={c}>{c || '전체'}</option>
        ))}
      </select>
    </div>
  );
}
