import { TestBed } from '@angular/core/testing';

import { Payment } from './PaymentService';

describe('Payment', () => {
  let service: Payment;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(Payment);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
