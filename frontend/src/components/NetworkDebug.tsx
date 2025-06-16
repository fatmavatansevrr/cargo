import React, { useState, useEffect } from 'react';

interface NetworkRequest {
  id: string;
  method: string;
  url: string;
  status?: number;
  data?: any;
  response?: any;
  error?: any;
  timestamp: number;
}

// XMLHttpRequest tipini genişlet
interface CustomXMLHttpRequest extends XMLHttpRequest {
  _method?: string;
  _url?: string;
}

const NetworkDebug: React.FC = () => {
  const [requests, setRequests] = useState<NetworkRequest[]>([]);
  const [isOpen, setIsOpen] = useState(false);

  useEffect(() => {
    // Override fetch ve axios için logging
    const originalFetch = window.fetch;
    const originalXhrOpen = XMLHttpRequest.prototype.open;
    const originalXhrSend = XMLHttpRequest.prototype.send;

    // Fetch override
    window.fetch = async (...args) => {
      const [url, options] = args;
      const requestId = Date.now().toString();
      
      const request: NetworkRequest = {
        id: requestId,
        method: options?.method || 'GET',
        url: url.toString(),
        timestamp: Date.now(),
        data: options?.body ? JSON.parse(options.body.toString()) : null,
      };

      setRequests(prev => [...prev, request]);

      try {
        const response = await originalFetch(...args);
        const responseData = await response.clone().json().catch(() => null);
        
        setRequests(prev => prev.map(req => 
          req.id === requestId 
            ? { ...req, status: response.status, response: responseData }
            : req
        ));

        return response;
      } catch (error) {
        setRequests(prev => prev.map(req => 
          req.id === requestId 
            ? { ...req, error: error }
            : req
        ));
        throw error;
      }
    };

    // XMLHttpRequest override for axios
    XMLHttpRequest.prototype.open = function(method, url) {
      const self = this as CustomXMLHttpRequest;
      self._method = method;
      self._url = url.toString();
      return originalXhrOpen.apply(this, arguments as any);
    };

    XMLHttpRequest.prototype.send = function(data) {
      const self = this as CustomXMLHttpRequest;
      const requestId = Date.now().toString();
      
      const request: NetworkRequest = {
        id: requestId,
        method: self._method || 'GET',
        url: self._url || '',
        timestamp: Date.now(),
        data: data && typeof data === 'string' ? JSON.parse(data) : null,
      };

      setRequests(prev => [...prev, request]);

      this.addEventListener('load', () => {
        try {
          const responseData = JSON.parse(this.responseText);
          setRequests(prev => prev.map(req => 
            req.id === requestId 
              ? { ...req, status: this.status, response: responseData }
              : req
          ));
        } catch (e) {
          setRequests(prev => prev.map(req => 
            req.id === requestId 
              ? { ...req, status: this.status, response: this.responseText }
              : req
          ));
        }
      });

      this.addEventListener('error', () => {
        setRequests(prev => prev.map(req => 
          req.id === requestId 
            ? { ...req, error: 'Network Error' }
            : req
        ));
      });

      return originalXhrSend.apply(this, arguments as any);
    };

    return () => {
      window.fetch = originalFetch;
      XMLHttpRequest.prototype.open = originalXhrOpen;
      XMLHttpRequest.prototype.send = originalXhrSend;
    };
  }, []);

  const getStatusColorClass = (status?: number) => {
    if (!status) return 'bg-gray-500';
    if (status >= 200 && status < 300) return 'bg-green-500';
    if (status >= 400) return 'bg-red-500';
    return 'bg-yellow-500';
  };

  if (!isOpen) {
    return (
      <div className="fixed bottom-4 right-4 z-50">
        <button
          onClick={() => setIsOpen(true)}
          className="bg-blue-500 hover:bg-blue-600 text-white px-4 py-2 rounded-lg shadow-lg transition-colors"
        >
          Network Debug ({requests.length})
        </button>
      </div>
    );
  }

  return (
    <div className="fixed bottom-4 right-4 w-96 bg-white rounded-lg shadow-xl border border-gray-200 z-50 max-h-96 overflow-hidden">
      {/* Header */}
      <div className="bg-gray-50 px-4 py-3 border-b border-gray-200 flex items-center justify-between">
        <h3 className="text-lg font-semibold text-gray-900">Network Debug</h3>
        <button
          onClick={() => setIsOpen(false)}
          className="text-gray-400 hover:text-gray-600 transition-colors"
        >
          ✕
        </button>
      </div>

      {/* Content */}
      <div className="p-4 overflow-y-auto max-h-80">
        {requests.length === 0 && (
          <div className="bg-blue-50 border border-blue-200 rounded-lg p-3">
            <p className="text-blue-800 text-sm">Henüz network isteği gönderilmedi</p>
          </div>
        )}

        {requests.slice(-5).reverse().map((request) => (
          <div key={request.id} className="mb-3 border border-gray-200 rounded-lg overflow-hidden">
            {/* Request Header */}
            <div className="bg-gray-50 px-3 py-2 flex items-center gap-2">
              <span className="bg-blue-500 text-white px-2 py-1 rounded text-xs font-medium">
                {request.method}
              </span>
              <span className={`${getStatusColorClass(request.status)} text-white px-2 py-1 rounded text-xs font-medium`}>
                {request.status || 'Pending'}
              </span>
              <span className="text-sm text-gray-700 truncate flex-1">
                {request.url}
              </span>
            </div>

            {/* Request Details */}
            <div className="p-3 space-y-3">
              {request.data && (
                <div>
                  <h4 className="text-sm font-semibold text-gray-900 mb-1">Request Data:</h4>
                  <pre className="text-xs bg-gray-100 p-2 rounded overflow-x-auto">
                    {JSON.stringify(request.data, null, 2)}
                  </pre>
                </div>
              )}
              
              {request.response && (
                <div>
                  <h4 className="text-sm font-semibold text-gray-900 mb-1">Response:</h4>
                  <pre className="text-xs bg-gray-100 p-2 rounded overflow-x-auto">
                    {JSON.stringify(request.response, null, 2)}
                  </pre>
                </div>
              )}
              
              {request.error && (
                <div>
                  <h4 className="text-sm font-semibold text-red-600 mb-1">Error:</h4>
                  <p className="text-sm text-red-600">
                    {request.error.toString()}
                  </p>
                </div>
              )}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

export default NetworkDebug;