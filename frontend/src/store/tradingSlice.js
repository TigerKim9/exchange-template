import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import { tradingAPI } from '../services/api';

export const fetchTradingPairs = createAsyncThunk(
  'trading/fetchTradingPairs',
  async (_, { rejectWithValue }) => {
    try {
      const response = await tradingAPI.getTradingPairs();
      return response.data;
    } catch (error) {
      return rejectWithValue(error.response?.data || 'Failed to fetch trading pairs');
    }
  }
);

export const fetchUserOrders = createAsyncThunk(
  'trading/fetchUserOrders',
  async (_, { rejectWithValue }) => {
    try {
      const response = await tradingAPI.getUserOrders();
      return response.data;
    } catch (error) {
      return rejectWithValue(error.response?.data || 'Failed to fetch orders');
    }
  }
);

export const createOrder = createAsyncThunk(
  'trading/createOrder',
  async (orderData, { rejectWithValue }) => {
    try {
      const response = await tradingAPI.createOrder(orderData);
      return response.data;
    } catch (error) {
      return rejectWithValue(error.response?.data || 'Failed to create order');
    }
  }
);

const tradingSlice = createSlice({
  name: 'trading',
  initialState: {
    tradingPairs: [],
    orders: [],
    selectedPair: null,
    loading: false,
    error: null,
  },
  reducers: {
    setSelectedPair: (state, action) => {
      state.selectedPair = action.payload;
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(fetchTradingPairs.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchTradingPairs.fulfilled, (state, action) => {
        state.loading = false;
        state.tradingPairs = action.payload;
        if (action.payload.length > 0 && !state.selectedPair) {
          state.selectedPair = action.payload[0];
        }
      })
      .addCase(fetchTradingPairs.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      })
      .addCase(fetchUserOrders.fulfilled, (state, action) => {
        state.orders = action.payload;
      });
  },
});

export const { setSelectedPair } = tradingSlice.actions;
export default tradingSlice.reducer;
