import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import { walletAPI } from '../services/api';

export const fetchWallets = createAsyncThunk(
  'wallet/fetchWallets',
  async (_, { rejectWithValue }) => {
    try {
      const response = await walletAPI.getWallets();
      return response.data;
    } catch (error) {
      return rejectWithValue(error.response?.data || 'Failed to fetch wallets');
    }
  }
);

export const deposit = createAsyncThunk(
  'wallet/deposit',
  async (data, { rejectWithValue }) => {
    try {
      const response = await walletAPI.deposit(data);
      return response.data;
    } catch (error) {
      return rejectWithValue(error.response?.data || 'Deposit failed');
    }
  }
);

export const withdraw = createAsyncThunk(
  'wallet/withdraw',
  async (data, { rejectWithValue }) => {
    try {
      const response = await walletAPI.withdraw(data);
      return response.data;
    } catch (error) {
      return rejectWithValue(error.response?.data || 'Withdrawal failed');
    }
  }
);

const walletSlice = createSlice({
  name: 'wallet',
  initialState: {
    wallets: [],
    loading: false,
    error: null,
  },
  reducers: {},
  extraReducers: (builder) => {
    builder
      .addCase(fetchWallets.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchWallets.fulfilled, (state, action) => {
        state.loading = false;
        state.wallets = action.payload;
      })
      .addCase(fetchWallets.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      });
  },
});

export default walletSlice.reducer;
