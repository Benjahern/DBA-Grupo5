import axios from 'axios';
import api from './http-common.js';

const IPAPI_URL = 'https://ipapi.co/json/';

export async function fetchUserLocation() {
  try {
    const { data } = await axios.get(IPAPI_URL, { timeout: 4000 });
    if (typeof data.latitude === 'number' && typeof data.longitude === 'number') {
      return {
        latitude: data.latitude,
        longitude: data.longitude,
        city: data.city,
        country: data.country_name,
      };
    }
    return null;
  } catch {
    return null;
  }
}

export async function getRegionLatencies(lat, lng) {
  const { data } = await api.get('/api/regions/ping', { params: { lat, lng } });
  return data;
}
