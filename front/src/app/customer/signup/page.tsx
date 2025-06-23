import RegisterForm from "@/components/customer/join/RegisterForm";


export default function RegisterPage() {
  return (
    <main className="min-h-screen flex items-center justify-center bg-gray-100">
      <div className="bg-white p-6 rounded shadow-md w-full max-w-lg">
        <h1 className="text-2xl font-bold mb-4">회원가입</h1>
        <RegisterForm />
      </div>
    </main>
  );
}
