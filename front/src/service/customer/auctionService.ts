import apiClient from "@/lib/apiClient";


export const fetchCustomerAuctions = async () => {
  const response = await apiClient.get('customer/auctions');
  return response.data;
};
