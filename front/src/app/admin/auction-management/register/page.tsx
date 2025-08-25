"use client";
import React, { useState } from "react";

export default function AuctionRegisterPage() {
  const [form, setForm] = useState({
    name: "",
    image: null as File | null,
    startPrice: "",
    buyNowPrice: "",
    startDate: "",
    endDate: "",
    description: ""
  });

  if (typeof window !== 'undefined' && !localStorage.getItem('adminToken')) {
    window.location.replace('/admin/login');
    return null;
  }

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target;
    setForm(prev => ({ ...prev, [name]: value }));
  };

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setForm(prev => ({ ...prev, image: e.target.files ? e.target.files[0] : null }));
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    alert("경매 등록 기능은 추후 구현 예정입니다!\n입력값: " + JSON.stringify(form, null, 2));
  };

  return (
    <div style={{ display: "flex", justifyContent: "center", alignItems: "center", minHeight: "80vh", background: "#f4f4f4" }}>
      <form onSubmit={handleSubmit} style={{ background: "#fff", borderRadius: 12, boxShadow: "0 2px 8px #eee", padding: 36, minWidth: 420, maxWidth: 480, width: "100%" }}>
        <h2 style={{ fontWeight: "bold", fontSize: 28, marginBottom: 28, textAlign: "center" }}>경매등록</h2>
        <div style={{ marginBottom: 18 }}>
          <label style={{ fontWeight: 500 }}>상품명</label>
          <input name="name" value={form.name} onChange={handleChange} required style={{ width: "100%", marginTop: 6, padding: 8, border: "1px solid #ccc", borderRadius: 6 }} />
        </div>
        <div style={{ marginBottom: 18 }}>
          <label style={{ fontWeight: 500 }}>이미지</label>
          <input type="file" accept="image/*" onChange={handleFileChange} style={{ display: "block", marginTop: 6 }} />
          <span style={{ fontSize: 13, color: "#888" }}>{form.image ? form.image.name : "선택된 파일 없음"}</span>
        </div>
        <div style={{ display: "flex", gap: 12, marginBottom: 18 }}>
          <div style={{ flex: 1 }}>
            <label style={{ fontWeight: 500 }}>시작가</label>
            <input name="startPrice" value={form.startPrice} onChange={handleChange} required style={{ width: "100%", marginTop: 6, padding: 8, border: "1px solid #ccc", borderRadius: 6 }} />
          </div>
          <div style={{ flex: 1 }}>
            <label style={{ fontWeight: 500 }}>즉시구매가</label>
            <input name="buyNowPrice" value={form.buyNowPrice} onChange={handleChange} style={{ width: "100%", marginTop: 6, padding: 8, border: "1px solid #ccc", borderRadius: 6 }} />
          </div>
        </div>
        <div style={{ display: "flex", gap: 12, marginBottom: 18 }}>
          <div style={{ flex: 1 }}>
            <label style={{ fontWeight: 500 }}>시작일</label>
            <input name="startDate" type="date" value={form.startDate} onChange={handleChange} required style={{ width: "100%", marginTop: 6, padding: 8, border: "1px solid #ccc", borderRadius: 6 }} />
          </div>
          <div style={{ flex: 1 }}>
            <label style={{ fontWeight: 500 }}>마감일</label>
            <input name="endDate" type="date" value={form.endDate} onChange={handleChange} required style={{ width: "100%", marginTop: 6, padding: 8, border: "1px solid #ccc", borderRadius: 6 }} />
          </div>
        </div>
        <div style={{ marginBottom: 24 }}>
          <label style={{ fontWeight: 500 }}>설명</label>
          <textarea name="description" value={form.description} onChange={handleChange} rows={3} style={{ width: "100%", marginTop: 6, padding: 8, border: "1px solid #ccc", borderRadius: 6, resize: "vertical" }} />
        </div>
        <button type="submit" style={{ width: "100%", padding: "12px 0", background: "#222", color: "#fff", border: "none", borderRadius: 6, fontWeight: "bold", fontSize: 18, cursor: "pointer" }}>
          등록
        </button>
      </form>
    </div>
  );
} 