import { useEffect, useState } from 'react'

function App() {
  const [backendStatus, setBackendStatus] = useState('Checking...')

  useEffect(() => {
    fetch('/api/health')
      .then((response) => {
        if (!response.ok) {
          throw new Error('Backend unavailable')
        }
        return response.json()
      })
      .then((data) => setBackendStatus(data.status))
      .catch(() => setBackendStatus('Disconnected'))
  }, [])

  return (
    <main style={{ padding: '32px', fontFamily: 'Arial' }}>
      <h1>RabbitMQ Task Processing Demo</h1>

      <p>Learning RabbitMQ with Spring Boot and React.</p>

      <h3>Application Status</h3>
      <p>Backend: {backendStatus}</p>

      <p>RabbitMQ integration will be added next.</p>
    </main>
  )
}

export default App