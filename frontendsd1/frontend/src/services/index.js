import authService from './authService';
import usersService from './usersService';
import devicesService from './devicesService';
import monitoringService from './monitoringService';

function attachTokenToAll(token) {
  authService.setToken(token);
  usersService.setToken(token);
  devicesService.setToken(token);
  monitoringService.setToken(token);
}

function clearTokenFromAll() {
  authService.clearToken();
  usersService.clearToken();
  devicesService.clearToken();
  monitoringService.clearToken();
}

export { authService, usersService, devicesService, monitoringService, attachTokenToAll, clearTokenFromAll };
