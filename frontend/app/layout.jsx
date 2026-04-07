import 'bootstrap/dist/css/bootstrap.min.css';
import './globals.css';
import Header from './components/header';
import Footer from './components/footer';
import { AuthProvider } from './components/authProvider';
import { ToastProvider } from './components/toastProvider';

export default function RootLayout({ children }) {
  return (
    <html lang="en">
      <body className="container">
        <AuthProvider>
          <ToastProvider>
            <Header />
            {children}
            <Footer />
          </ToastProvider>
        </AuthProvider>
      </body>
    </html>
  );
}