import type { Metadata } from "next";
import { Geist, Geist_Mono } from "next/font/google";
import "./globals.css";
import Head from "next/head";
import Footer from "@/components/customer/common/Footer";
import { Providers } from "./providers";
import ClientNavbarWrapper from "@/components/customer/common/ClientNavbarWrapper";
import ClientChatbotWrapper from "@/components/customer/common/ClientChatbotWrapper";

const geistSans = Geist({
  variable: "--font-geist-sans",
  subsets: ["latin"],
});

const geistMono = Geist_Mono({
  variable: "--font-geist-mono",
  subsets: ["latin"],
});

export const metadata: Metadata = {
  title: "Realive",
  description: "Realive",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en">
    <Head>
      <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
      <link rel="icon" href="/favicon.ico" />
    </Head>
      <body
        className={`
          ${geistSans.variable}
          ${geistMono.variable}
          antialiased
          min-h-screen
          flex flex-col
          bg-white
        `}
      >
        <ClientNavbarWrapper />

        <Providers>
          <main className="flex-grow">{children}</main>
        </Providers>

        <Footer />
        <ClientChatbotWrapper />
      </body>
    </html>
  );
}
