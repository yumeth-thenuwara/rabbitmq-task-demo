
import { useCallback, useEffect, useState } from 'react'

function App() {
  const [text, setText] = useState('')
  const [tasks, setTasks] = useState([])
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState('')

  const loadTasks = useCallback(async () => {
    const response = await fetch('/api/tasks')

    if (!response.ok) {
      throw new Error('Unable to load tasks')
    }

    const data = await response.json()
    setTasks(data)
  }, [])

  useEffect(() => {
    const refresh = () => {
      loadTasks().catch((err) => setError(err.message))
    }

    refresh()

    // Refresh task statuses every 2 seconds
    const interval = setInterval(refresh, 2000)

    return () => clearInterval(interval)
  }, [loadTasks])

  async function submitTask(event) {
    event.preventDefault()

    if (!text.trim()) return

    setSubmitting(true)
    setError('')

    try {
      const response = await fetch('/api/tasks', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ text }),
      })

      if (!response.ok) {
        throw new Error('Unable to submit task')
      }

      setText('')
      await loadTasks()
    } catch (err) {
      setError(err.message)
    } finally {
      setSubmitting(false)
    }
  }

  function statusColor(status) {
    if (status === 'COMPLETED') return '#15803d'
    if (status === 'PROCESSING') return '#2563eb'
    if (status === 'FAILED') return '#dc2626'
    return '#a16207'
  }

  return (
    <main style={{
      maxWidth: 960,
      margin: '40px auto',
      padding: '0 20px',
    }}>
      <h1>RabbitMQ Task Processing Demo</h1>
      <p>Submit text and watch RabbitMQ process it asynchronously.</p>

      <form onSubmit={submitTask} style={{
        background: 'white',
        padding: 24,
        borderRadius: 10,
        marginBottom: 28,
      }}>
        <h2>Submit a New Task</h2>

        <textarea
          value={text}
          onChange={(e) => setText(e.target.value)}
          placeholder="Enter text to process..."
          rows={4}
          maxLength={2000}
          style={{
            width: '100%',
            padding: 12,
            border: '1px solid #ccc',
            borderRadius: 6,
            resize: 'vertical',
          }}
        />

        <div style={{ marginTop: 12 }}>
          <button
            type="submit"
            disabled={submitting || !text.trim()}
            style={{
              padding: '10px 20px',
              background: '#2563eb',
              color: 'white',
              border: 'none',
              borderRadius: 6,
            }}
          >
            {submitting ? 'Submitting...' : 'Submit Task'}
          </button>
        </div>
      </form>

      {error && (
        <p role="alert" style={{ color: '#dc2626' }}>
          {error}
        </p>
      )}

      <h2>Processing Jobs</h2>

      <div style={{ overflowX: 'auto' }}>
        <table style={{
          width: '100%',
          borderCollapse: 'collapse',
          background: 'white',
        }}>
          <thead>
            <tr>
              {['Task ID', 'Text', 'Status', 'Result'].map((heading) => (
                <th key={heading} style={{
                  padding: 12,
                  textAlign: 'left',
                  borderBottom: '1px solid #ddd',
                }}>
                  {heading}
                </th>
              ))}
            </tr>
          </thead>

          <tbody>
            {tasks.map((task) => (
              <tr key={task.id}>
                <td style={{ padding: 12 }}>
                  {task.id.slice(0, 8)}
                </td>
                <td style={{ padding: 12 }}>
                  {task.text.length > 80
                    ? task.text.slice(0, 80) + '...'
                    : task.text}
                </td>
                <td style={{
                  padding: 12,
                  fontWeight: 'bold',
                  color: statusColor(task.status),
                }}>
                  {task.status}
                </td>
                <td style={{ padding: 12 }}>
                  {task.result || '-'}
                </td>
              </tr>
            ))}
          </tbody>
        </table>

        {tasks.length === 0 && (
          <p>No tasks submitted yet.</p>
        )}
      </div>
    </main>
  )
}

export default App
