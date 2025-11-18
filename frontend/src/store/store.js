import { configureStore } from '@reduxjs/toolkit';
import authReducer from './authSlice';
import walletReducer from './walletSlice';
import tradingReducer from './tradingSlice';

export const store = configureStore({
  reducer: {
    auth: authReducer,
    wallet: walletReducer,
    trading: tradingReducer,
  },
});
