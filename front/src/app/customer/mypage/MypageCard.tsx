export default function MypageCard({
                                       title,
                                       children,
                                   }: {
    title: string;
    children: React.ReactNode;
}) {
    return (
        <div className="bg-white shadow rounded-lg p-4">
            <h2 className="text-lg font-semibold mb-2">{title}</h2>
            {children}
        </div>
    );
}