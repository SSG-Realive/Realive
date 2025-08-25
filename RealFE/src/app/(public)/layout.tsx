import Link from "next/link";
import "./globals.css";      // Tailwind 초기화가 들어 있는 파일

export const metadata = { title: "REALIVE" };

export default function PublicLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="ko">
      <body className="flex min-h-screen flex-col">
        <header className="bg-slate-800 text-white p-4 flex gap-4">
          <Link href="/">Home</Link>
          <Link href="/admin">Admin</Link>
          <Link href="/seller">Seller</Link>
          <Link href="/customer">Customer</Link>
        </header>

        <main className="flex-1 p-6">{children}</main>

        <footer className="p-4 text-center text-xs text-gray-500">
          ©2025 REALIVE
        </footer>
      </body>
    </html>
  );
}