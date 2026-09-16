import React from 'react';
import { createRoot } from 'react-dom/client';
import './styles.css';

const metrics = [
  ['Payment success', '99.96%', '+0.08%'],
  ['PayShap p95', '1.24s', '-180ms'],
  ['Reconciliation breaks', '3', '-7'],
  ['Fraud prevented', 'R 842k', '+12%']
];

function App() {
  return <main>
    <aside><div className="brand"><span>U</span> Ubuntu Bank</div><nav>Overview<br/>Payments<br/>Customers<br/>Fraud & AML<br/>Reconciliation<br/>Platform Health</nav><div className="badge">PRODUCTION · ZA</div></aside>
    <section>
      <header><div><small>OPERATIONS CONTROL CENTRE</small><h1>Digital banking, clearly operated.</h1><p>Live business health across payments, risk and settlement.</p></div><button>Generate incident brief</button></header>
      <div className="metrics">{metrics.map(([k,v,d])=><article key={k}><label>{k}</label><strong>{v}</strong><em>{d}</em></article>)}</div>
      <div className="grid">
        <article className="panel wide"><div className="title"><h2>Payment throughput</h2><span>Last 24 hours</span></div><div className="chart">{[35,52,43,68,61,84,73,92,78,88,67,82,94,76,89,97].map((h,i)=><i key={i} style={{height:`${h}%`}} />)}</div><div className="legend"><span>PayShap 64%</span><span>Internal 22%</span><span>EFT / RTC 14%</span></div></article>
        <article className="panel"><div className="title"><h2>Rail health</h2></div>{[['PayShap','Healthy'],['Internal','Healthy'],['EFT','Healthy'],['RTC','Degraded'],['SAMOS','Healthy']].map(([a,b])=><div className="rail" key={a}><b>{a}</b><span className={b==='Degraded'?'warn':''}>{b}</span></div>)}</article>
        <article className="panel wide"><div className="title"><h2>Operational exceptions</h2><span>Prioritised by customer impact</span></div><table><tbody>
          <tr><td><b>RTC acknowledgement latency</b><small>Rail adapter · 14 min</small></td><td><span className="sev amber">SEV-2</span></td><td>Payments SRE</td></tr>
          <tr><td><b>Ledger reconciliation variance</b><small>3 unmatched items · R 4,218</small></td><td><span className="sev">CONTROL</span></td><td>Finance Ops</td></tr>
          <tr><td><b>KYC provider retry backlog</b><small>42 applications · no SLA breach</small></td><td><span className="sev blue">WATCH</span></td><td>Customer Ops</td></tr>
        </tbody></table></article>
      </div>
    </section>
  </main>
}
createRoot(document.getElementById('root')!).render(<React.StrictMode><App/></React.StrictMode>);

