export default function footer() {
  return (
    <>
      <div id='footer' className='container border-top mt-3 align-middle'>
        <div className="row mt-3">
          <div className="col-md-4">
            <p style={{maxWidth:400}}>Copyright 2026 Beacon Team</p>
          </div>
          <div className="col-md-4">
            <p style={{maxWidth:400}}></p>
          </div>
          <div className="col-md-4">
          {/*  <ul className='float-end list-unstyled'>
              <li><a className='p-3' href='../auth/register'>Register</a></li>
              <li><a className='p-3' href='../auth/login'>Login</a></li>
              <li><a className='p-3' href='../dashboard'>My Dashboard</a></li>
              <li><a className='p-3' href='/'>About Beacon</a></li>

            </ul>*/}
          </div>
        </div>
      </div>
    </>
  );
}