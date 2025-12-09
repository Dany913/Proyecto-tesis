import './App.css';
import { useState, useEffect } from 'react';
import axios from 'axios';

function App() {
  const [files, setFiles] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showFiles, setShowFiles] = useState(false);

  const fetchFiles = async () => {
    setLoading(true);
    try {
      const response = await axios.get('http://localhost:8086/api/fileManager/files');
      setFiles(response.data);
      setShowFiles(true);
    } catch (error) {
      console.error('Error al obtener archivos:', error);
      setShowFiles(true);
    } finally {
      setLoading(false);
    }
  };

  return (
      <div className="App">
        <div className="container">
          <div className="card">
            <h2>Cargar</h2>
            <p>Carga el registro</p>
          </div>
          <div className="card">
            <h2>Descargar</h2>
            <p>Descargar Registro</p>
          </div>
          <div className="card" onClick={fetchFiles} style={{ cursor: 'pointer' }}>
            <h2>Lista</h2>
            {loading ? (
                <p>Cargando...</p>
            ) : showFiles ? (
                <ul>
                  {files.map((file, index) => (
                      <li key={index}>{file.name}</li>
                  ))}
                </ul>
            ) : (
                <p>Haz clic para cargar registros</p>
            )}
          </div>
        </div>
      </div>
  );
}

export default App;
