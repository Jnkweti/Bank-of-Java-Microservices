export interface AccountModel {
  id: string;
  customerId: string;
  accName: string;
  type: 'CHECKING' | 'SAVINGS';
  status: 'ACTIVE' | 'CLOSED';
  balance: string;
  accountNumber: string;
  openedDate: string;
  lastUpdated: string;
  interestRate: string;
}

export interface CustomerModel {
  id: string;
  firstName: string;
  lastName: string;
  email: string;
  address: string;
  birthDate: string;
  registerDate: string;
}

export interface PaymentModel {
  id: string;
  fromAccountId: string;
  toAccountId: string;
  amount: string;
  status: 'COMPLETED' | 'FAILED' | 'PENDING';
  type: 'TRANSFER' | 'DEPOSIT' | 'WITHDRAWAL';
  description: string;
  createdAt: string;
  updatedAt: string;
}

export interface AuthResponse {
  accessToken: string;
}

export interface CustomerIdResponse {
  id: string;
}