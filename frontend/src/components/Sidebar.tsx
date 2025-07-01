import React from 'react';
import {
  Drawer,
  List,
  ListItem,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  Box,
  Typography,
  Divider,
  Chip,
  useTheme,
  useMediaQuery,
} from '@mui/material';
import {
  Dashboard as DashboardIcon,
  LocalShipping as ShippingIcon,
  Search as SearchIcon,
  Add as AddIcon,
  Analytics as AnalyticsIcon,
  People as PeopleIcon,
  Settings as SettingsIcon,
  Notifications as NotificationsIcon,
  History as HistoryIcon,
  Assignment as AssignmentIcon,
  AccountCircle as ProfileIcon,
} from '@mui/icons-material';
import { useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { UserRole } from '../types';
import { hasPermission, Permission } from '../utils/rolePermissions';

interface SidebarProps {
  open: boolean;
  onClose: () => void;
}

interface MenuItem {
  text: string;
  icon: React.ReactNode;
  path: string;
  permission?: Permission;
  badge?: number;
}

// Helper functions for user role
function getRoleColor(role: UserRole): "default" | "primary" | "secondary" | "error" | "info" | "success" | "warning" {
  switch (role) {
    case UserRole.ADMIN:
      return 'error';
    case UserRole.CARRIER:
      return 'warning';
    case UserRole.SHIPPER:
      return 'primary';
    case UserRole.CUSTOMER:
      return 'secondary';
    default:
      return 'default';
  }
}

function getRoleText(role: UserRole): string {
  switch (role) {
    case UserRole.ADMIN:
      return 'Admin';
    case UserRole.CARRIER:
      return 'Taşıyıcı';
    case UserRole.SHIPPER:
      return 'Gönderici';
    case UserRole.CUSTOMER:
      return 'Müşteri';
    default:
      return 'Kullanıcı';
  }
}

const Sidebar: React.FC<SidebarProps> = ({ open, onClose }) => {
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('md'));
  const location = useLocation();
  const navigate = useNavigate();
  const { user } = useAuth();

  // Menü öğeleri - permission göre filtreleme
  const menuItems: MenuItem[] = [
    {
      text: 'Dashboard',
      icon: <DashboardIcon />,
      path: '/dashboard',
      permission: undefined, // Dashboard herkes için erişilebilir
    },
    {
      text: 'Gönderi Takibi',
      icon: <SearchIcon />,
      path: '/tracking',
      permission: Permission.TRACK_SHIPMENTS,
    },
    {
      text: 'Yeni Gönderi',
      icon: <AddIcon />,
      path: '/shipments/new',
      permission: Permission.CREATE_SHIPMENT,
    },
    {
      text: 'Gönderilerim',
      icon: <ShippingIcon />,
      path: '/shipments',
    },
    {
      text: 'Raporlar',
      icon: <AnalyticsIcon />,
      path: '/reports',
      permission: Permission.VIEW_REPORTS,
    },
    {
      text: 'Kullanıcı Yönetimi',
      icon: <PeopleIcon />,
      path: '/admin/users',
      permission: Permission.MANAGE_USERS,
    },
    {
      text: 'Bildirimler',
      icon: <NotificationsIcon />,
      path: '/notifications',
      permission: Permission.VIEW_NOTIFICATIONS,
      badge: 3,
    },
    {
      text: 'Profil',
      icon: <ProfileIcon />,
      path: '/profile',
    },
  ];

  // Kullanıcı yetkilerine göre menü filtreleme
  const filteredMenuItems = menuItems.filter(item => 
    !item.permission || (user?.role && hasPermission(user.role, item.permission))
  );

  const handleItemClick = (path: string) => {
    navigate(path);
    if (isMobile) {
      onClose();
    }
  };

  const drawerWidth = 280;

  const drawerContent = (
    <Box sx={{ width: drawerWidth, height: '100%', display: 'flex', flexDirection: 'column' }}>
      {/* Logo area */}
      <Box sx={{ p: 3, borderBottom: 1, borderColor: 'divider' }}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
          <ShippingIcon sx={{ color: 'primary.main', fontSize: 32 }} />
          <Box>
            <Typography variant="h6" sx={{ fontWeight: 700, color: 'primary.main' }}>
              CargoTrack
            </Typography>
            <Typography variant="caption" sx={{ color: 'text.secondary' }}>
              Kargo Takip Sistemi
            </Typography>
          </Box>
        </Box>
      </Box>

      {/* Kullanıcı bilgisi */}
      {user && (
        <Box sx={{ p: 2, borderBottom: 1, borderColor: 'divider' }}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
            <Box
              sx={{
                width: 48,
                height: 48,
                borderRadius: '50%',
                bgcolor: 'primary.main',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                color: 'white',
                fontWeight: 600,
                fontSize: '1.1rem',
              }}
            >
              {user.firstName.charAt(0)}{user.lastName.charAt(0)}
            </Box>
            <Box sx={{ flex: 1, minWidth: 0 }}>
              <Typography variant="subtitle1" sx={{ fontWeight: 600 }} noWrap>
                {user.firstName} {user.lastName}
              </Typography>
              <Typography variant="body2" sx={{ color: 'text.secondary' }} noWrap>
                {user.email}
              </Typography>
              <Chip
                label={getRoleText(user.role)}
                size="small"
                color={getRoleColor(user.role)}
                sx={{ mt: 0.5, height: 20, fontSize: '0.7rem' }}
              />
            </Box>
          </Box>
        </Box>
      )}

      {/* Navigation items */}
      <Box sx={{ flex: 1, overflow: 'auto' }}>
        <List sx={{ py: 1 }}>
          {filteredMenuItems.map((item, index) => {
            const isActive = location.pathname === item.path;
            
            return (
              <ListItem key={item.text} disablePadding sx={{ px: 1 }}>
                <ListItemButton
                  onClick={() => handleItemClick(item.path)}
                  sx={{
                    borderRadius: 2,
                    mb: 0.5,
                    backgroundColor: isActive ? 'primary.main' : 'transparent',
                    color: isActive ? 'white' : 'text.primary',
                    '&:hover': {
                      backgroundColor: isActive ? 'primary.dark' : 'action.hover',
                    },
                    '& .MuiListItemIcon-root': {
                      color: isActive ? 'white' : 'text.secondary',
                    },
                  }}
                >
                  <ListItemIcon sx={{ minWidth: 40 }}>
                    {item.icon}
                  </ListItemIcon>
                  <ListItemText 
                    primary={item.text} 
                    primaryTypographyProps={{
                      fontSize: '0.9rem',
                      fontWeight: isActive ? 600 : 400,
                    }}
                  />
                  {item.badge && (
                    <Chip
                      label={item.badge}
                      size="small"
                      color="error"
                      sx={{ 
                        height: 20, 
                        minWidth: 20,
                        '& .MuiChip-label': {
                          px: 0.5,
                          fontSize: '0.7rem',
                        }
                      }}
                    />
                  )}
                </ListItemButton>
              </ListItem>
            );

            // Her 4 öğeden sonra ayırıcı ekle
            if ((index + 1) % 4 === 0 && index < filteredMenuItems.length - 1) {
              return (
                <React.Fragment key={`divider-${index}`}>
                  <Divider sx={{ my: 1, mx: 2 }} />
                </React.Fragment>
              );
            }
          })}
        </List>
      </Box>

      {/* Footer */}
      <Box sx={{ p: 2, borderTop: 1, borderColor: 'divider' }}>
        <Typography variant="caption" sx={{ color: 'text.secondary', textAlign: 'center', display: 'block' }}>
          © 2024 CargoTrack
        </Typography>
        <Typography variant="caption" sx={{ color: 'text.secondary', textAlign: 'center', display: 'block' }}>
          v1.0.0
        </Typography>
      </Box>
    </Box>
  );

  return (
    <Drawer
      variant={isMobile ? 'temporary' : 'persistent'}
      anchor="left"
      open={open}
      onClose={onClose}
      ModalProps={{
        keepMounted: true, // Better open performance on mobile
      }}
      sx={{
        '& .MuiDrawer-paper': {
          width: drawerWidth,
          boxSizing: 'border-box',
          border: 'none',
          boxShadow: '2px 0 8px rgba(0,0,0,0.1)',
        },
      }}
    >
      {drawerContent}
    </Drawer>
  );
};

export default Sidebar; 