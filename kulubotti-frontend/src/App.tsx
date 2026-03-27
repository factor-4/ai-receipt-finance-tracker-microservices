import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { useAuth } from './context/AuthContext';
import LoginPage from './pages/LoginPage';
import ExpenseDashboard from './pages/ExpenseDashboard';

// A "Gatekeeper" component
const ProtectedRoute = ({ children }: { children: React.ReactNode }) => {
  const { isAuthenticated } = useAuth();


  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  return <>{children}</>;
};

function App() {
  const { isAuthenticated } = useAuth();
  console.log('iatuthenticate value in app tex ', isAuthenticated);

  return (
    <BrowserRouter>
      <Routes>
        
        <Route path="/login" element={<LoginPage />} />

        <Route path="/dashboard" element={
          <ProtectedRoute>
            <ExpenseDashboard />
          </ProtectedRoute>
        } />

        {/* Only use the ternary for the "Catch-all" redirect */}
        <Route path="*" element={<Navigate to={isAuthenticated ? "/dashboard" : "/login"} />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;