import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { useAuth } from './context/AuthContext';
import LoginPage from './pages/LoginPage';

// A "Gatekeeper" component
const ProtectedRoute = ({ children }: { children: React.ReactNode }) => {
  const { isAuthenticated } = useAuth();
  
  // If not logged in, send them to /login
  if (!isAuthenticated) {
    return <Navigate to="/login" /> ;
  }
  
  return <>{children}</>;
};

function App() {
  const { isAuthenticated } = useAuth();

  return (
    <BrowserRouter>
      <Routes>
        {/* Public Route */}
        <Route path="/login" element={
          isAuthenticated ? <Navigate to="/dashboard" /> : <LoginPage />
        } />

        {/* Protected Routes (Hidden behind the gate) */}
        <Route path="/dashboard" element={
          <ProtectedRoute>
            <div className="p-10 text-white">
              <h1 className="text-4xl font-bold">Welcome to your Dashboard!</h1>
              <p className="mt-4">If you see this, your Java JWT is working.</p>
            </div>
          </ProtectedRoute>
        } />

        {/* Default redirect */}
        <Route path="*" element={<Navigate to={isAuthenticated ? "/dashboard" : "/login"} />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;