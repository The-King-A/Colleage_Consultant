import express from 'express'

import cors from 'cors'

import { volunteerBriefHtml, hollandBriefHtml, schoolCompareHtml } from './templates.js'



const app = express()

const PORT = process.env.PORT || 3001



app.use(cors())

app.use(express.json({ limit: '1mb' }))



app.get('/health', (_req, res) => {

  res.json({ status: 'ok', service: 'report-service', stack: 'Node.js Express', version: '1.1.0' })

})



/** 志愿冲稳保简报 */

app.post('/api/v1/report/volunteer-brief', (req, res) => {

  const html = volunteerBriefHtml(req.body || {})

  res.json({

    title: req.body?.title || '志愿规划简报',

    html,

    generatedAt: new Date().toISOString(),

    stack: 'Node.js Express',

  })

})



/** 霍兰德测评报告（统一导出，替代各页面内联 HTML） */

app.post('/api/v1/report/holland-brief', (req, res) => {

  const html = hollandBriefHtml(req.body || {})

  res.json({

    title: '霍兰德测评报告',

    html,

    generatedAt: new Date().toISOString(),

    stack: 'Node.js Express',

  })

})



/** 院校对比表导出 */

app.post('/api/v1/report/school-compare', (req, res) => {

  const html = schoolCompareHtml(req.body || {})

  res.json({

    title: req.body?.title || '院校对比报告',

    html,

    generatedAt: new Date().toISOString(),

    stack: 'Node.js Express',

  })

})



app.listen(PORT, () => {

  console.log(`Report service http://localhost:${PORT}`)

})


