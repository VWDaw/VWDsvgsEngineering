/*
                      FoilSim III  - Airfoil  mode
                          Undergrad Version
   
                           A Java Applet
               to perform Kutta-Joukowski Airfoil analysis
                including drag from wind tunnel tests

                     Version 1.4d   - 21 Mar 11

                              Written by 

                               Tom Benson
                       NASA Glenn Research Center

                                 and
              
                               Anthony Vila
                          Vanderbilt University

>                                NOTICE
>This software is in the Public Domain.  It may be freely copied and used in
>non-commercial products, assuming proper credit to the author is given.  IT
>MAY NOT BE RESOLD.  If you want to use the software for commercial
>products, contact the author.
>No copyright is claimed in the United States under Title 17, U. S. Code.
>This software is provided "as is" without any warranty of any kind, either
>express, implied, or statutory, including, but not limited to, any warranty
>that the software will conform to specifications, any implied warranties of
>merchantability, fitness for a particular purpose, and freedom from
>infringement, and any warranty that the documentation will conform to the
>program, or any warranty that the software will be error free.
>In no event shall NASA be liable for any damages, including, but not
>limited to direct, indirect, special or consequential damages, arising out
>of, resulting from, or in any way connected with this software, whether or
>not based on warranty, contract, tort or otherwise, whether or not injury
>was sustained by persons or property or otherwise, and whether or not loss
>was sustained from, or arose out of the results of, or use of, the software
>or services provided hereunder.
 
  New test -
             * include the drag
             * rename modules and change layout
             * change the color scheme on the plots
             * correct lift of ball from CurveBall
             * change pressure and velocity plots for stalled airfoil
             * add reynolds number calculation
               get moment coefficient - FoilSim IV
               put separation bubble on airfoil graphics - FoilSim IV

                                           TJB  22 Jun 10

  New Test B
           * -Build Drag Data Interpolator to incorporate drag results
	     * -Implement interpolator into program (create drag output results textbar)
           * -get drag of elliptical foils

                                           AJV 7/14/10
  New Test C
           *  add drag plots
           *  move plot selection to input side
           *  change to interpolated data
           *  add analysis panel - 
           *  make input and output buttons - not drop down
           *  add reynolds correction to airfoil - make optional
           *  add induced drag to drag calculation  - make optional
           *  add drag to gauge output
           *  add smooth ball / rough ball option 
           *  release as FoilSim III
           *  move metric/imperial units to control panel - user input
           *  add some standard shapes to Shape input with buttons
           *  fix up a symmetry problem for negative camber and drag 
           *  fix up a plotting problem for drag involving the induced drag
           *  change number of sig fig on graphs - small airfoils

           * add volume of the wing to the printed output - option "Geometry"
                                             TJB 21 Mar 11
                                             
*/

import java.awt.*;
import java.lang.Math ;

public class Foil extends java.applet.Applet {
 
   static double convdr = 3.1415926/180. ;
   static double pid2 = 3.1415926/2.0 ;
   static double rval,ycval,xcval,gamval,alfval,thkval,camval,chrd,clift;
   static double dragCoeff,drag,liftOverDrag,reynolds,viscos ;
   static double alfd,thkd,camd,dragco ;
   static double thkinpt,caminpt ;                 /* MODS 10 Sep 99 */
   static double leg,teg,lem,tem;
   static double usq,vsq,alt,altmax,area,armax,armin ;
   static double chord,span,aspr,arold,chrdold,spnold ; /* Mod 13 Jan 00 */
   static double g0,q0,ps0,pt0,ts0,rho,rlhum,temf,presm ;
   static double lyg,lrg,lthg,lxgt,lygt,lrgt,lthgt,lxgtc,lygtc;
   static double lxm,lym,lxmt,lymt,vxdir;/* MOD 20 Jul */
   static double deltb,xflow ;             /* MODS  20 Jul 99 */
   static double delx,delt,vfsd,spin,spindr,yoff,radius ;
   static double vel,pres,lift,side,omega,radcrv,relsy,angr;

   static double rg[][]  = new double[20][40] ; 
   static double thg[][] = new double[20][40] ; 
   static double xg[][]  = new double[20][40] ; 
   static double yg[][]  = new double[20][40] ; 
   static double xm[][]  = new double[20][40] ; 
   static double ym[][]  = new double[20][40] ; 
   static double xpl[][]  = new double[20][40] ; 
   static double ypl[][]  = new double[20][40] ; 
   static double xgc[][]  = new double[20][40] ;
   static double ygc[][]  = new double[20][40] ;
   static double xplg[][]  = new double[20][40] ;
   static double yplg[][]  = new double[20][40] ;
   static double plp[]   = new double[40] ;
   static double plv[]   = new double[40] ;

   int inptopt,outopt ;
   int nptc,npt2,nlnc,nln2,rdflag,browflag,probflag,anflag;
   int foil,flflag,lunits,lftout,planet,dragOut ;
   int displ,viewflg,dispp,dout,doutb,antim,ancol,sldloc; 
   int calcrange,arcor,indrag,recor,bdragflag ;
       /* units data */
   static double vmn,almn,angmn,vmx,almx,angmx ;
   static double camn,thkmn,camx,thkmx ;
   static double chrdmn,spanmn,armn,chrdmx,spanmx,armx ;
   static double radmn,spinmn,radmx,spinmx ;
   static double vconv,vmax ;
   static double pconv,pmax,pmin,lconv,rconv,fconv,fmax,fmaxb;
   int lflag,gflag,plscale,nond;
       /*  plot & probe data */
   static double fact,xpval,ypval,pbval,factp;
   static double prg,pthg,pxg,pyg,pxm,pym,pxpl,pypl ;
   int pboflag,xt,yt,ntikx,ntiky,npt,xtp,ytp ;
   int xt1,yt1,xt2,yt2,spanfac ;
   int lines,nord,nabs,ntr ;
   static double begx,endx,begy,endy ;
   static String labx,labxu,laby,labyu ;
   static double pltx[][]  = new double[3][40] ;
   static double plty[][]  = new double[3][40] ;
   static double plthg[]  = new double[2] ;

   Solver solve ;
   Viewer view ;
   Con con ;
   In in ;
   Out out ;
   CardLayout layin,layout,layplt ;
   Image offImg1 ;
   Graphics off1Gg ;
   Image offImg2 ;
   Graphics off2Gg ;
   Image offImg3 ;
   Graphics off3Gg ;

   public void init() {
     int i;
     solve = new Solver() ;

     offImg1 = createImage(this.size().width,
                      this.size().height) ;
     off1Gg = offImg1.getGraphics() ;
     offImg2 = createImage(this.size().width,
                      this.size().height) ;
     off2Gg = offImg2.getGraphics() ;
     offImg3 = createImage(this.size().width,
                      this.size().height) ;
     off3Gg = offImg3.getGraphics() ;

     setLayout(new GridLayout(2,2,5,5)) ;

     solve.setDefaults () ;

     view  = new Viewer(this) ;
     con = new Con(this) ;
     in = new In(this) ;
     out = new Out(this) ;

     add(view) ;
     add(con) ;
     add(in) ;
     add(out) ;
 
     solve.getFreeStream ();
     computeFlow () ;
     view.start() ;
     out.plt.start() ;
  }
 
  public Insets insets() {
     return new Insets(10,10,10,10) ;
  }

  public void computeFlow() { 

      solve.getFreeStream () ;

     if (flflag == 1) {
         solve.getCirc ();                   /* get circulation */
     }

     solve.getGeom () ;
     solve.genFlow () ;

     if (foil <= 3)
         {
         reynolds = vfsd/vconv * chord/lconv * rho / viscos ;
         }
     else
         {
         reynolds = vfsd/vconv * 2 * radius/lconv * rho / viscos;
         }
 
     solve.getProbe() ;

     thkd = thkinpt ;
     camd = caminpt ;
     alfd = alfval ;
//   attempt to fix symmetry problem
     if (camd < 0.0) alfd = - alfval ;
//
     solve.getDrag(clift); 
     dragCoeff = dragco ;
 
     loadOut() ;

     out.plt.loadPlot() ;
  }

  public int filter0(double inumbr) {
        //  output only to .
       int number ;
       int intermed ;
 
       number = (int) (inumbr);
       return number ;
  }

  public float filter1(double inumbr) {
     //  output only to .1
       float number ;
       int intermed ;
 
       intermed = (int) (inumbr * 10.) ;
       number = (float) (intermed / 10. );
       return number ;
  }
 
  public float filter3(double inumbr) {
     //  output only to .001
       float number ;
       int intermed ;
 
       intermed = (int) (inumbr * 1000.) ;
       number = (float) (intermed / 1000. );
       return number ;
  }
 
  public float filter5(double inumbr) {
     //  output only to .00001
       float number ;
       int intermed ;
 
       intermed = (int) (inumbr * 100000.) ;
       number = (float) (intermed / 100000. );
       return number ;
  }
 
  public float filter9(double inumbr) {
     //  output only to .000000001
       float number ;
       int intermed ;
 
       intermed = (int) (inumbr * 1000000000.) ;
       number = (float) (intermed / 1000000000. );
       return number ;
  }
 
  public void setUnits() {   // Switching Units
       double ovs,chords,spans,aros,chos,spos,rads ;
       double alts,ares ;

       alts = alt / lconv ;
       chords = chord / lconv ;
       spans = span / lconv ;
       ares = area /lconv/lconv ;
       aros = arold /lconv/lconv ;
       chos = chrdold / lconv ;
       spos = spnold / lconv ;
       ovs = vfsd / vconv ;
       rads = radius / lconv ;

       switch (lunits) {
          case 0: {                             /* English */
            lconv = 1.;                      /*  feet    */
            vconv = .6818; vmax = 250.;   /*  mph  */
            if (planet == 2) vmax = 50. ;
            fconv = 1.0; fmax = 100000.; fmaxb = .5;  /* pounds   */
            pconv = 14.7  ;                   /* lb/sq in */
            break;
          }
          case 1: {                             /* Metric */
            lconv = .3048;                    /* meters */
            vconv = 1.097; vmax = 400. ;   /* km/hr  */
            if (planet == 2) vmax = 80. ;
            fconv = 4.448 ; fmax = 500000.; fmaxb = 2.5; /* newtons */
            pconv = 101.3 ;               /* kilo-pascals */
            break ;
          }
       }
 
       alt = alts * lconv ;
       chord = chords * lconv ;
       span = spans * lconv ;
       area = ares * lconv * lconv ;
       arold = aros * lconv * lconv ;
       chrdold = chos * lconv ;
       spnold = spos * lconv ;
       vfsd  = ovs * vconv;
       radius  = rads * lconv;

       return ;
  }

  public void loadInput() {   // load the input panels
       int i1,i2,i3,i4,i5,i6 ;
       double v1,v2,v3,v4,v5,v6 ;
       float fl1,fl2,fl3,fl4,fl5,fl6 ;
                  //  dimensional
       if (lunits == 0) {
           in.siz.inl.l1.setText("Chord-ft") ;
           in.siz.inl.l2.setText("Span-ft") ;
           in.siz.inl.l3.setText("Area-sq ft") ;
           in.flt.inl.l1.setText("Speed-mph") ;
           in.cyl.inl.l2.setText("Radius ft") ;
           in.cyl.inl.l3.setText("Span ft") ;
           if(planet == 2)in.flt.inl.l2.setText("Depth-ft") ;
           if(planet != 2)in.flt.inl.l2.setText("Altitude-ft");
       }
       if (lunits == 1) {
           in.siz.inl.l1.setText("Chord-m") ;
           in.siz.inl.l2.setText("Span-m") ;
           in.siz.inl.l3.setText("Area-sq m") ;
           in.flt.inl.l1.setText("Speed-km/h") ;
           in.cyl.inl.l2.setText("Radius m") ;
           in.cyl.inl.l3.setText("Span m") ;
           if(planet == 2)in.flt.inl.l2.setText("Depth-m") ;
           if(planet != 2)in.flt.inl.l2.setText("Altitude-m");
       }
       v1 = chord ;
       chrdmn = 0.1*lconv;   chrdmx = 20.1*lconv ;
       v2 = span ;
       spanmn = 0.1*lconv;   spanmx = 125.1*lconv ;
       if (planet == 2) {
           chrdmx = 5.1*lconv ;
           spanmx = 10.1*lconv ;
       }
       v3 = area ;
       armn = armin*lconv*lconv; armx = armax*lconv*lconv ;
       v4 = vfsd ;
       vmn = 0.0;   vmx= vmax ;
       v5 = alt ;
       almn = 0.0;  almx = altmax*lconv ;
       v6 = radius ;
       radmn = .05*lconv;  radmx = 5.0*lconv ;
       aspr = span/chord ;
       spanfac = (int)(2.0*fact*aspr*.3535) ;

       fl1 = (float) v1 ;
       fl2 = (float) v2 ;
       fl3 = (float) v3 ;
       fl4 = filter3(v4) ;
       fl5 = (float) v5 ;
       fl6 = (float) v6 ;
   
       in.siz.inl.f1.setText(String.valueOf(fl1)) ;
       in.siz.inl.f2.setText(String.valueOf(fl2)) ;
       in.siz.inl.f3.setText(String.valueOf(fl3)) ;
       in.flt.inl.f1.setText(String.valueOf(fl4)) ;
       in.flt.inl.f2.setText(String.valueOf(fl5)) ;
       in.cyl.inl.f2.setText(String.valueOf(fl6)) ;
       in.cyl.inl.f3.setText(String.valueOf(fl2)) ;
   
       i1 = (int) (((v1 - chrdmn)/(chrdmx-chrdmn))*1000.) ;
       i2 = (int) (((v2 - spanmn)/(spanmx-spanmn))*1000.) ;
       i3 = (int) (((v3 - armn)/(armx-armn))*1000.) ;
       i4 = (int) (((v4 - vmn)/(vmx-vmn))*1000.) ;
       i5 = (int) (((v5 - almn)/(almx-almn))*1000.) ;
       i6 = (int) (((v6 - radmn)/(radmx-radmn))*1000.) ;

       in.siz.inr.sld1.s1.setValue(i1) ;
       in.siz.inr.sld2.s2.setValue(i2) ;
       in.siz.inr.sld3.s3.setValue(i3) ;
       in.flt.inr.s1.setValue(i4) ;
       in.flt.inr.s2.setValue(i5) ;
       in.cyl.inr.s2.setValue(i6) ;
       in.cyl.inr.s3.setValue(i2) ;
                //  non-dimensional
       v1 = caminpt ;
       v2 = thkinpt ;
       v3 = alfval ;
       v4 = spin*60.0 ;

       fl1 = (float) v1 ;
       fl2 = (float) v2 ;
       fl3 = (float) v3 ;
       fl4 = (float) v4 ;

       in.shp.inl.f1.setText(String.valueOf(fl1)) ;
       in.shp.inl.f2.setText(String.valueOf(fl2)) ;
       in.shp.inl.f3.setText(String.valueOf(fl3)) ;
       in.cyl.inl.f1.setText(String.valueOf(fl4)) ;

       i1 = (int) (((v1 - camn)/(camx-camn))*1000.) ;
       i2 = (int) (((v2 - thkmn)/(thkmx-thkmn))*1000.) ;
       i3 = (int) (((v3 - angmn)/(angmx-angmn))*1000.) ;
       i4 = (int) (((v4 - spinmn)/(spinmx-spinmn))*1000.) ;
     
       in.shp.inr.s1.setValue(i1) ;
       in.shp.inr.s2.setValue(i2) ;
       in.shp.inr.s3.setValue(i3) ;
       in.cyl.inr.s1.setValue(i4) ;

                // generating cylinder
       v1 = rval ;
       v2 = xcval ;
       v3 = ycval ;
       v4 = gamval ;

       fl1 = filter3(v1) ;
       fl2 = filter3(v2) ;
       fl3 = filter3(v3) ;
       fl4 = filter3(v4) ;

       in.genp.inl.f1.setText(String.valueOf(fl1)) ;
       in.genp.inl.f2.setText(String.valueOf(fl2)) ;
       in.genp.inl.f3.setText(String.valueOf(fl3)) ;
       in.genp.inl.f4.setText(String.valueOf(fl4)) ;

       i1 = (int) (((v1 - 1.0)/(4.0))*1000.) ;
       i2 = (int) (((v2 + 1.0)/(2.0))*1000.) ;
       i3 = (int) (((v3 + 1.0)/(2.0))*1000.) ;
       i4 = (int) (((v4 + 2.0)/(4.0))*1000.) ;

       in.genp.inr.s1.setValue(i1) ;
       in.genp.inr.s2.setValue(i2) ;
       in.genp.inr.s3.setValue(i3) ;
       in.genp.inr.s4.setValue(i4) ;

       computeFlow() ;
       return ;
  }

  public void loadOut() {   // output routine
     String outunit ;

     outunit = " lbs" ;
     if (lunits == 1) outunit = " N" ;

     if (foil <= 3) {     // mapped airfoil
                          // stall model
 
        if (lftout == 1) {
          con.outlft.setText(String.valueOf(filter3(clift))) ;
        }
        if (lftout == 0) {
          lift = clift * q0 * area / lconv / lconv ; /* lift in lbs */
          lift = lift * fconv ;
          if (Math.abs(lift) <= 10.0) {
             con.outlft.setText(String.valueOf(filter3(lift)) + outunit) ;
          }
          if (Math.abs(lift) > 10.0) {
             con.outlft.setText(String.valueOf(filter0(lift)) + outunit) ;
          }
        }
     }
     if (foil >= 4) {     // cylinder and ball

        lift = rho * vfsd/vconv * gamval * vfsd/vconv * span/lconv; // lift lbs
//    ball
//        if (foil == 5) lift = lift * 3.1415926 / 2.0 ; 
        if (foil == 5) lift = lift * 4.0 * rval / (span/lconv) / 3.0 ; 
        lift = lift * fconv ;
        clift = (lift/fconv) / ( q0 *  area/lconv/lconv) ;
        if (Math.abs(lift) <= 10.0) {
           con.outlft.setText(String.valueOf(filter3(lift)) + outunit) ;
        }
        if (Math.abs(lift) > 10.0) {
           con.outlft.setText(String.valueOf(filter0(lift)) + outunit) ;
        }
        if (lftout == 1) {
          clift = (lift/fconv) / ( q0 *  area/lconv/lconv) ;
          con.outlft.setText(String.valueOf(filter3(clift))) ;
        }
     }

     if (dragOut == 1) 
         {
         con.outDrag.setText(String.valueOf(filter3(dragCoeff))) ;
         }
     if (dragOut == 0) {
         drag = dragCoeff * q0 * area / lconv / lconv ; /* drag in lbs */
         drag = drag * fconv ;
         if (Math.abs(drag) <= 10.0) {
           con.outDrag.setText(String.valueOf(filter3(drag)) + outunit) ;
         }
         if (Math.abs(drag) > 10.0) {
           con.outDrag.setText(String.valueOf(filter0(drag)) + outunit) ;
         }
     }

     liftOverDrag = clift/dragCoeff;
     if (liftOverDrag <= 1000.) con.outLD.setText(String.valueOf(filter3(liftOverDrag)));
     if (liftOverDrag  > 1000.) con.outLD.setText(" - ");
 
     switch (lunits)  {
       case 0: {                             /* English */
           in.flt.inl.o1.setText(String.valueOf(filter3(ps0/144.))) ;
           in.flt.inl.lo1.setText("Press. lb/in2") ;
           in.flt.inr.inr2.o2.setText(String.valueOf(filter0(ts0 - 460.))) ;
           in.flt.inr.inr2.lo2.setText("Temp. F") ;
           in.flt.inl.lo3.setText("Dens. slug/ft^3") ;
           in.flt.inl.o3.setText(String.valueOf(filter5(rho))) ;
           in.flt.inr.inr3.lo4.setText("Visc. slug/ft-s") ;
           in.flt.inr.inr3.o4.setText(String.valueOf(filter9(viscos))) ;
           in.siz.inl.o4.setText(String.valueOf(filter3(aspr))) ;
           break;
        }
        case 1: {                             /* Metric */
           in.flt.inl.o1.setText(String.valueOf(filter3(101.3/14.7*ps0/144.))) ;
           in.flt.inl.lo1.setText("Press. kPa") ;
           in.flt.inr.inr2.o2.setText(String.valueOf(filter0(ts0*5.0/9.0 - 273.1))) ;
           in.flt.inr.inr2.lo2.setText("Temp. C") ;
           in.flt.inl.o3.setText(String.valueOf(filter3(rho*515.4))) ;
           in.flt.inl.lo3.setText("Dens. kg/m^3") ;
           in.flt.inr.inr3.o4.setText(String.valueOf(filter9(viscos*47.87))) ;
           in.flt.inr.inr3.lo4.setText("Visc. kg/m-s") ;
           in.siz.inl.o4.setText(String.valueOf(filter3(aspr))) ;
           break ;
        }
     }

     con.outReynolds.setText(String.valueOf(filter0(reynolds))) ;

     return ;
  }

  public void loadProbe() {   // probe output routine

     pbval = 0.0 ;
     if (pboflag == 1) pbval = vel * vfsd ;           // velocity
     if (pboflag == 2) pbval = ((ps0 + pres * q0)/2116.) * pconv ; // pressure
 
     out.prb.r.l2.repaint() ;
     return ;
  }

  class Solver {
 
     Solver () {
     }

     public void setDefaults() {

        arcor = 1 ;
        indrag = 1 ;
        recor = 1 ;
        bdragflag = 1;  // smooth ball
        planet = 0 ;
        lunits = 0 ;
        lftout = 0 ;
        inptopt = 0 ;
        outopt = 0 ;
        nlnc = 15 ;
        nln2 = nlnc/2 + 1 ;
        nptc = 37 ;
        npt2 = nptc/2 + 1 ;
        deltb = .5 ;
        foil = 1 ;
        flflag = 1;
        thkval = .5 ;
        thkinpt = 12.5 ;                   /* MODS 10 SEP 99 */
        camval = 0.0 ;
        caminpt = 0.0 ;
        alfval = 5.0 ;
        gamval = 0.0 ;
        radius = 1.0 ;
        spin = 0.0 ;
        spindr = 1.0 ;
        rval = 1.0 ;
        ycval = 0.0 ;
        xcval = 0.0 ;
        displ   = 1 ;                            
        viewflg = 0 ;
        dispp = 0 ;
        calcrange = 0 ;
        dout = 0 ;
        doutb = 0 ;

        dragCoeff = 0;
 
        xpval = 2.1;
        ypval = -.5 ;
        pboflag = 0 ;
        xflow = -10.0;                             /* MODS  20 Jul 99 */

        pconv = 14.7;
        pmin = .5 ;
        pmax = 1.0 ;
        fconv = 1.0 ;
        fmax = 100000. ;
        fmaxb = .50 ;
        vconv = .6818 ;
        vfsd = 100. ;
        vmax = 250. ;
        lconv = 1.0 ;

        alt = 0.0 ;
        altmax = 50000. ;
        chrdold = chord = 5.0 ;
        spnold = span = 20.0 ;
        aspr = 4.0 ;
        arold = area = 100.0 ;
        armax = 2500.01 ;
        armin = .01 ;                 /* MODS 9 SEP 99 */
 
        xt = 170;  yt = 105; fact = 30.0 ;
        sldloc = 50 ;
        xtp = 95; ytp = 165; factp = 30.0 ;
        spanfac = (int)(2.0*fact*aspr*.3535) ;
        xt1 = xt + spanfac ;
        yt1 = yt - spanfac ;
        xt2 = xt - spanfac;
        yt2 = yt + spanfac ;
        plthg[1] = 0.0 ;
 
        probflag = 0 ;
        anflag = 1 ;
        vmn = 0.0;     vmx = 250.0 ;
        almn = 0.0;    almx = 50000.0 ;
        angmn = -20.0; angmx = 20.0 ;
        camn = -20.0;  camx = 20.0 ;
        thkmn = 1.0; thkmx = 20.0 ;
        chrdmn = .1 ;  chrdmx = 20.1 ;
        spanmn = .1 ;  spanmx = 125.1 ;
        armn = .01 ;  armx = 2500.01 ;
        spinmn = -1500.0;   spinmx = 1500.0 ;
        radmn = .05;   radmx = 5.0 ;

        return ;
     }

     public void getFreeStream() {    //  free stream conditions
       double hite,pvap,rgas,gama,mu0 ;       /* MODS  19 Jan 00  whole routine*/

       g0 = 32.2 ;
       rgas = 1718. ;                /* ft2/sec2 R */
       gama = 1.4 ;
       hite = alt/lconv ;
       mu0 = .000000362 ;
       if (planet == 0) {    // Earth  standard day
         if (hite <= 36152.) {           // Troposphere
            ts0 = 518.6 - 3.56 * hite/1000. ;
            ps0 = 2116. * Math.pow(ts0/518.6,5.256) ;
         }
         if (hite >= 36152. && hite <= 82345.) {   // Stratosphere
            ts0 = 389.98 ;
            ps0 = 2116. * .2236 *
                 Math.exp((36000.-hite)/(53.35*389.98)) ;
         }
         if (hite >= 82345.) {
            ts0 = 389.98 + 1.645 * (hite-82345)/1000. ;
            ps0 = 2116. *.02456 * Math.pow(ts0/389.98,-11.388) ;
         }
         temf = ts0 - 459.6 ;
         if (temf <= 0.0) temf = 0.0 ;                    
/* Eq 1:6A  Domasch  - effect of humidity 
         rlhum = 0.0 ;
         presm = ps0 * 29.92 / 2116. ;
         pvap = rlhum*(2.685+.00353*Math.pow(temf,2.245));
         rho = (ps0 - .379*pvap)/(rgas * ts0) ; 
*/
         rho = ps0/(rgas * ts0) ;
         viscos = mu0 * 717.408/(ts0 + 198.72)*Math.pow(ts0/518.688,1.5) ;
       }

       if (planet == 1) {   // Mars - curve fit of orbiter data
         rgas = 1149. ;                /* ft2/sec2 R */
         gama = 1.29 ;

         if (hite <= 22960.) {
            ts0 = 434.02 - .548 * hite/1000. ;
            ps0 = 14.62 * Math.pow(2.71828,-.00003 * hite) ;
         }
         if (hite > 22960.) {
            ts0 = 449.36 - 1.217 * hite/1000. ;
            ps0 = 14.62 * Math.pow(2.71828,-.00003 * hite) ;
         }
         rho = ps0/(rgas*ts0) ;
         viscos = mu0 * 717.408/(ts0 + 198.72)*Math.pow(ts0/518.688,1.5) ;
       }

       if (planet == 2) {   // water --  constant density
         hite = -alt/lconv ;
         ts0 = 520. ;
         rho = 1.94 ;
         ps0 = (2116. - rho * g0 * hite) ;
         mu0 = .0000272 ;
         viscos = mu0 * 717.408/(ts0 + 198.72)*Math.pow(ts0/518.688,1.5) ;
       }

       if (planet == 3) {   // specify air temp and pressure 
          rho = ps0/(rgas*ts0) ;
          viscos = mu0 * 717.408/(ts0 + 198.72)*Math.pow(ts0/518.688,1.5) ;
       }

       if (planet == 4) {   // specify fluid density and viscosity
          ps0 = 2116. ;
       }

       q0  = .5 * rho * vfsd * vfsd / (vconv * vconv) ;
       pt0 = ps0 + q0 ;

       return ;
     }

     public void getGeom() {   // geometry
       double thet,rdm,thtm ;
       int index;

       for (index =1; index <= nptc; ++index) {
           thet = (index -1)*360./(nptc-1) ;
           xg[0][index] = rval * Math.cos(convdr * thet) + xcval ;
           yg[0][index] = rval * Math.sin(convdr * thet) + ycval ;
           rg[0][index] = Math.sqrt(xg[0][index]*xg[0][index] +
                                yg[0][index]*yg[0][index])  ;
           thg[0][index] = Math.atan2(yg[0][index],xg[0][index])/convdr;
           xm[0][index] = (rg[0][index] + 1.0/rg[0][index])*
                    Math.cos(convdr*thg[0][index]) ;
           ym[0][index] = (rg[0][index] - 1.0/rg[0][index])*
                    Math.sin(convdr*thg[0][index]) ;
           rdm = Math.sqrt(xm[0][index]*xm[0][index] +
                           ym[0][index]*ym[0][index])  ;
           thtm = Math.atan2(ym[0][index],xm[0][index])/convdr;
           xm[0][index] = rdm * Math.cos((thtm - alfval)*convdr);
           ym[0][index] = rdm * Math.sin((thtm - alfval)*convdr);
           getVel(rval,thet) ;
           plp[index] = ((ps0 + pres * q0)/2116.217) * pconv ;
           plv[index] = vel * vfsd ;
           xgc[0][index] = rval * Math.cos(convdr * thet) + xcval ;
           ygc[0][index] = rval * Math.sin(convdr * thet) + ycval ;
       }

       xt1 = xt + spanfac ;
       yt1 = yt - spanfac ;
       xt2 = xt - spanfac;
       yt2 = yt + spanfac ;

       return ;
     }

     public void getCirc() {   // circulation from Kutta condition
       double thet,rdm,thtm ;
       double beta;
       int index;

       xcval = 0.0 ;
       switch (foil)  {
          case 1:  {                  /* Juokowski geometry*/
              ycval = camval / 2.0 ;
              rval = thkval/4.0 +Math.sqrt(thkval*thkval/16.0+ycval*ycval +1.0);
              xcval = 1.0 - Math.sqrt(rval*rval - ycval*ycval) ;
              beta = Math.asin(ycval/rval)/convdr ;     /* Kutta condition */
              gamval = 2.0*rval*Math.sin((alfval+beta)*convdr) ;
              break ;
          }
          case 2:  {                  /* Elliptical geometry*/
              ycval = camval / 2.0 ;
              rval = thkval/4.0 + Math.sqrt(thkval*thkval/16.0+ycval*ycval+1.0);
              beta = Math.asin(ycval/rval)/convdr ;    /* Kutta condition */
              gamval = 2.0*rval*Math.sin((alfval+beta)*convdr) ;
              break ;
          }
          case 3:  {                  /* Plate geometry*/
              ycval = camval / 2.0 ;
              rval = Math.sqrt(ycval*ycval+1.0);
              beta = Math.asin(ycval/rval)/convdr ;    /* Kutta condition */
              gamval = 2.0*rval*Math.sin((alfval+beta)*convdr) ;
              break ;
          }
          case 4: {         /* get circulation for rotating cylnder */
              rval = radius/lconv ;
              gamval = 4.0 * 3.1415926 * 3.1415926 *spin * rval * rval
                                 / (vfsd/vconv) ;
              gamval = gamval * spindr ;
              ycval = .0001 ;
              break ;
          }
          case 5: {         /* get circulation for rotating ball */
              rval = radius/lconv ;
              gamval = 4.0 * 3.1415926 * 3.1415926 *spin * rval * rval
                                 / (vfsd/vconv) ;
              gamval = gamval * spindr ;
              ycval = .0001 ;
              break ;
          }
       }

       if (foil <=3 && anflag == 2) gamval = 0.0 ;

                             // geometry
       for (index =1; index <= nptc; ++index) {
           thet = (index -1)*360./(nptc-1) ;
           xg[0][index] = rval * Math.cos(convdr * thet) + xcval ;
           yg[0][index] = rval * Math.sin(convdr * thet) + ycval ;
           rg[0][index] = Math.sqrt(xg[0][index]*xg[0][index] +
                                yg[0][index]*yg[0][index])  ;
           thg[0][index] = Math.atan2(yg[0][index],xg[0][index])/convdr;
           xm[0][index] = (rg[0][index] + 1.0/rg[0][index])*
                    Math.cos(convdr*thg[0][index]) ;
           ym[0][index] = (rg[0][index] - 1.0/rg[0][index])*
                    Math.sin(convdr*thg[0][index]) ;
           rdm = Math.sqrt(xm[0][index]*xm[0][index] +
                           ym[0][index]*ym[0][index])  ;
           thtm = Math.atan2(ym[0][index],xm[0][index])/convdr;
           xm[0][index] = rdm * Math.cos((thtm - alfval)*convdr);
           ym[0][index] = rdm * Math.sin((thtm - alfval)*convdr);
           getVel(rval,thet) ;
           plp[index] = ((ps0 + pres * q0)/2116.) * pconv ;
           plv[index] = vel * vfsd ;
       }

       xt1 = xt + spanfac ;
       yt1 = yt - spanfac ;
       xt2 = xt - spanfac;
       yt2 = yt + spanfac ;

       return ;
     }

     public void genFlow() {   // generate flowfield
       double rnew,thet,psv,fxg;
       double stfact ;
       int k,index;
                              /* all lines of flow  except stagnation line*/
       for (k=1; k<=nlnc; ++k) {
         psv = -.5*(nln2-1) + .5*(k-1) ;
         fxg = xflow ;
         for (index =1; index <=nptc; ++ index) {
           solve.getPoints (fxg,psv) ;
           xg[k][index]  = lxgt ;
           yg[k][index]  = lygt ;
           rg[k][index]  = lrgt ;
           thg[k][index] = lthgt ;
           xm[k][index]  = lxmt ;
           ym[k][index]  = lymt ;
           if (anflag == 1) {           // stall model
              if (alfval > 10.0 && psv > 0.0) {
                   if (xm[k][index] > 0.0) {
                      ym[k][index] = ym[k][index -1] ;
                   }
              }
              if (alfval < -10.0 && psv < 0.0) {
                   if (xm[k][index] > 0.0) {
                      ym[k][index] = ym[k][index -1] ;
                   }
              }
           }
           solve.getVel(lrg,lthg) ;
           fxg = fxg + vxdir*deltb ;
           xgc[k][index]  = lxgtc ;
           ygc[k][index]  = lygtc ;
         }
       }
                                              /*  stagnation line */
       k = nln2 ;
       psv = 0.0 ;
                                              /*  incoming flow */
       for (index =1; index <= npt2; ++ index) {
           rnew = 10.0 - (10.0 - rval)*Math.sin(pid2*(index-1)/(npt2-1)) ;
           thet = Math.asin(.999*(psv - gamval*Math.log(rnew/rval))/
                                   (rnew - rval*rval/rnew)) ;
           fxg =  - rnew * Math.cos(thet) ;
           solve.getPoints (fxg,psv) ;
           xg[k][index]  = lxgt ;
           yg[k][index]  = lygt ;
           rg[k][index]  = lrgt ;
           thg[k][index] = lthgt ;
           xm[k][index]  = lxmt ;
           ym[k][index]  = lymt ;
           xgc[k][index]  = lxgtc ;
           ygc[k][index]  = lygtc ;
       }
                                              /*  downstream flow */
       for (index = 1; index <= npt2; ++ index) {
           rnew = 10.0 + .01 - (10.0 - rval)*Math.cos(pid2*(index-1)/(npt2-1)) ;
           thet = Math.asin(.999*(psv - gamval*Math.log(rnew/rval))/
                                      (rnew - rval*rval/rnew)) ;
           fxg =   rnew * Math.cos(thet) ;
           solve.getPoints (fxg,psv) ;
           xg[k][npt2+index]  = lxgt ;
           yg[k][npt2+index]  = lygt ;
           rg[k][npt2+index]  = lrgt ;
           thg[k][npt2+index] = lthgt ;
           xm[k][npt2+index]  = lxmt ;
           ym[k][npt2+index]  = lymt ;
           xgc[k][index]  = lxgtc ;
           ygc[k][index]  = lygtc ;
       }
                                              /*  stagnation point */
       xg[k][npt2]  = xcval ;
       yg[k][npt2]  = ycval ;
       rg[k][npt2]  = Math.sqrt(xcval*xcval+ycval*ycval) ;
       thg[k][npt2] = Math.atan2(ycval,xcval)/convdr ;
       xm[k][npt2]  = (xm[k][npt2+1] + xm[k][npt2-1])/2.0 ;
       ym[k][npt2]  = (ym[0][nptc/4+1] + ym[0][nptc/4*3+1])/2.0 ;
                                /*  compute lift coefficient */
       leg = xcval - Math.sqrt(rval*rval - ycval*ycval) ;
       teg = xcval + Math.sqrt(rval*rval - ycval*ycval) ;
       lem = leg + 1.0/leg ;
       tem = teg + 1.0/teg ;
       chrd = tem - lem ;
       clift = gamval*4.0*3.1415926/chrd ;

       stfact = 1.0 ;
       if (anflag == 1) {
            if (alfval > 10.0 ) {
               stfact = .5 + .1 * alfval - .005 * alfval * alfval ;
            }
            if (alfval < -10.0 ) {
               stfact = .5 - .1 * alfval - .005 * alfval * alfval ;
            }
            clift = clift * stfact ;
       }
               
       if (arcor == 1) {  // correction for low aspect ratio
            clift = clift /(1.0 + Math.abs(clift)/(3.14159*aspr)) ;
       }

       return ;
     }

     public void getPoints(double fxg, double psv) {   // flow in x-psi
       double radm,thetm ;                /* MODS  20 Jul 99  whole routine*/
       double fnew,ynew,yold,rfac,deriv ;
       double xold,xnew,thet ;
       double rmin,rmax ;
       int iter,isign;
                       /* get variables in the generating plane */
                           /* iterate to find value of yg */
       ynew = 10.0 ;
       yold = 10.0 ;
       if (psv < 0.0) ynew = -10.0 ;
       if (Math.abs(psv) < .001 && alfval < 0.0) ynew = rval ;
       if (Math.abs(psv) < .001 && alfval >= 0.0) ynew = -rval ;
       fnew = 0.1 ;
       iter = 1 ;
       while (Math.abs(fnew) >= .00001 && iter < 25) {
           ++iter ;
           rfac = fxg*fxg + ynew*ynew ;
           if (rfac < rval*rval) rfac = rval*rval + .01 ;
           fnew = psv - ynew*(1.0 - rval*rval/rfac)
                  - gamval*Math.log(Math.sqrt(rfac)/rval) ;
           deriv = - (1.0 - rval*rval/rfac)
               - 2.0 * ynew*ynew*rval*rval/(rfac*rfac)
               - gamval * ynew / rfac ;
           yold = ynew ;
           ynew = yold  - .5*fnew/deriv ;
       }
       lyg = yold ;
                                     /* rotate for angle of attack */
       lrg = Math.sqrt(fxg*fxg + lyg*lyg) ;
       lthg = Math.atan2(lyg,fxg)/convdr ;
       lxgt = lrg * Math.cos(convdr*(lthg + alfval)) ;
       lygt = lrg * Math.sin(convdr*(lthg + alfval)) ;
                              /* translate cylinder to generate airfoil */
       lxgtc = lxgt = lxgt + xcval ;
       lygtc = lygt = lygt + ycval ;
       lrgt = Math.sqrt(lxgt*lxgt + lygt*lygt) ;
       lthgt = Math.atan2(lygt,lxgt)/convdr ;
                               /*  Kutta-Joukowski mapping */
       lxm = (lrgt + 1.0/lrgt)*Math.cos(convdr*lthgt) ;
       lym = (lrgt - 1.0/lrgt)*Math.sin(convdr*lthgt) ;
                              /* tranforms for view fixed with free stream */
                /* take out rotation for angle of attack mapped and cylinder */
       radm = Math.sqrt(lxm*lxm+lym*lym) ;
       thetm = Math.atan2(lym,lxm)/convdr ;
       lxmt = radm*Math.cos(convdr*(thetm-alfval)) ;
       lymt = radm*Math.sin(convdr*(thetm-alfval)) ;

       lxgt = lxgt - xcval ;
       lygt = lygt - ycval ;
       lrgt = Math.sqrt(lxgt*lxgt + lygt*lygt)  ;
       lthgt = Math.atan2(lygt,lxgt)/convdr;
       lxgt = lrgt * Math.cos((lthgt - alfval)*convdr);
       lygt = lrgt * Math.sin((lthgt - alfval)*convdr);

       return ;
     }
 
     public void getVel(double rad, double theta) {  //velocity and pressure 
      double ur,uth,jake1,jake2,jakesq ;
      double xloc,yloc,thrad,alfrad ;

      thrad = convdr * theta ;
      alfrad = convdr * alfval ;
                                /* get x, y location in cylinder plane */
      xloc = rad * Math.cos(thrad) ;
      yloc = rad * Math.sin(thrad) ;
                                /* velocity in cylinder plane */
      ur  = Math.cos(thrad-alfrad)*(1.0-(rval*rval)/(rad*rad)) ;
      uth = -Math.sin(thrad-alfrad)*(1.0+(rval*rval)/(rad*rad))
                            - gamval/rad;
      usq = ur*ur + uth*uth ;
      vxdir = ur * Math.cos(thrad) - uth * Math.sin(thrad) ; // MODS  20 Jul 99 
                                /* translate to generate airfoil  */
      xloc = xloc + xcval ;
      yloc = yloc + ycval ;
                                   /* compute new radius-theta  */
      rad = Math.sqrt(xloc*xloc + yloc*yloc) ;
      thrad  = Math.atan2(yloc,xloc) ;
                                   /* compute Joukowski Jacobian  */
      jake1 = 1.0 - Math.cos(2.0*thrad)/(rad*rad) ;
      jake2 = Math.sin(2.0*thrad)/(rad*rad) ;
      jakesq = jake1*jake1 + jake2*jake2 ;
      if (Math.abs(jakesq) <= .01) jakesq = .01 ;  /* protection */
      vsq = usq / jakesq ;
          /* vel is velocity ratio - pres is coefficient  (p-p0)/q0   */
      if (foil <= 3) {
           vel = Math.sqrt(vsq) ;
           pres = 1.0 - vsq ;
      }
      if (foil >= 4) {
           vel = Math.sqrt(usq) ;
           pres = 1.0 - usq ;
      }
      return ;
    }

    public void getProbe () { /* all of the information needed for the probe */
      double prxg;
      int index;
                       /* get variables in the generating plane */
      if (Math.abs(ypval) < .01) ypval = .05 ;
      solve.getPoints (xpval,ypval) ;

      solve.getVel(lrg,lthg) ;
      loadProbe() ;

      pxg = lxgt ;
      pyg = lygt ;
      prg = lrgt ;
      pthg = lthgt ;
      pxm = lxmt ;
      pym = lymt ;
                                    /* smoke */
      if (pboflag == 3 ) {
        prxg = xpval ;
        for (index =1; index <=nptc; ++ index) {
          solve.getPoints (prxg,ypval) ;
          xg[19][index] = lxgt ;
          yg[19][index] = lygt ;
          rg[19][index] = lrgt ;
          thg[19][index] = lthgt ;
          xm[19][index] = lxmt ;
          ym[19][index] = lymt ;
          if (anflag == 1) {           // stall model
             if (xpval > 0.0) {
                if (alfval > 10.0 && ypval > 0.0) { 
                   ym[19][index] = ym[19][1] ;
                } 
                if (alfval < -10.0 && ypval < 0.0) {
                     ym[19][index] = ym[19][1] ;
                }
             }
             if (xpval < 0.0) {
                if (alfval > 10.0 && ypval > 0.0) { 
                   if (xm[19][index] > 0.0) {
                       ym[19][index] = ym[19][index-1] ;
                   }
                } 
                if (alfval < -10.0 && ypval < 0.0) {
                   if (xm[19][index] > 0.0) {
                     ym[19][index] = ym[19][index-1] ;
                   }
                }
             }
          }
          solve.getVel(lrg,lthg) ;
          prxg = prxg + vxdir*deltb ;
        }
      }
      return ;
    }

    public void getDrag(double cldin)     //Drag Interpolator
        {
        int index,ifound ;  
        double dragCam0Thk5, dragCam5Thk5, dragCam10Thk5, dragCam15Thk5, dragCam20Thk5;
        double dragCam0Thk10, dragCam5Thk10, dragCam10Thk10, dragCam15Thk10, dragCam20Thk10;
        double dragCam0Thk15, dragCam5Thk15, dragCam10Thk15, dragCam15Thk15, dragCam20Thk15;
        double dragCam0Thk20, dragCam5Thk20, dragCam10Thk20, dragCam15Thk20, dragCam20Thk20;
        double dragThk5, dragThk10, dragThk15, dragThk20;
        double dragCam0, dragCam5, dragCam10, dragCam15, dragCam20;  //used for the flat plate drag values
        double recyl[]  = {.1, .2, .4, .5, .6, .8, 1.0,
                              2.0, 4.0, 5.0, 6.0, 8.0, 10.0,
                          20.0, 40.0, 50.0, 60.0, 80.0, 100.0,
                         200.0, 400.0, 500.0, 600.0, 800.0, 1000.,
                        2000., 4000., 5000., 6000., 8000., 10000.,
                      100000.,200000.,400000.,500000.,600000.,800000.,1000000.,
                      2000000.,4000000.,5000000.,6000000.,8000000.,1000000000000. } ; 
        double cdcyl[]  = {70., 35., 20., 17., 15., 13., 10.,
                           7., 5.5, 5.0, 4.5, 4., 3.5,
                           3.0, 2.7, 2.5, 2.0, 2.0, 1.9,
                           1.6, 1.4, 1.2, 1.1, 1.1, 1.0, 
                           1.2, 1.4, 1.4, 1.5, 1.5, 1.6,
                           1.6, 1.4, .4, .28, .32, .4, .45,
                            .6, .8, .8, .85, .9, .9 } ; 
        double resps[]  = {.1, .2, .4, .5, .6, .8, 1.0,
                            2.0, 4.0, 5.0, 6.0, 8.0, 10.0,
                           20., 40., 50., 60., 80.0, 100.0,
                          200., 400., 500., 600., 800.0, 1000.,
                         2000., 4000., 5000., 6000., 8000., 10000.,
                        20000., 40000., 50000., 60000., 80000., 100000.,
                       200000., 400000., 500000., 600000., 800000., 1000000.,
                      2000000., 4000000., 5000000., 6000000., 8000000., 1000000000000. } ; 

        double cdsps[]  = {270., 110., 54., 51., 40., 35., 28.,
                           15., 8.5, 7.5, 6.0, 5.4, 4.9,
                           3.1, 1.9, 1.8, 1.5, 1.3, 1.1,
                           0.81, 0.6, 0.58, 0.56, 0.5, 0.49, 
                           0.40, 0.41, 0.415, 0.42, 0.43, 0.44,
                           0.44, 0.45, 0.455, 0.46, 0.47, 0.48, 
                           0.47, 0.10, 0.098, 0.1, 0.15, 0.19, 
                           0.30, 0.35, 0.370, 0.4, 0.40, 0.42 } ; 
        double cdspr[]  = {270., 110., 54., 51., 40., 35., 28.,
                           15., 8.5, 7.5, 6.0, 5.4, 4.9,
                           3.1, 1.9, 1.8, 1.5, 1.3, 1.1,
                           0.81, 0.6, 0.58, 0.56, 0.5, 0.49, 
                           0.40, 0.41, 0.415, 0.42, 0.43, 0.44,
                           0.44, 0.45, 0.455, 0.46, 0.42, 0.15, 
                           0.27, 0.33, 0.35, 0.37, 0.38, 0.39, 
                           0.40, 0.41, 0.41, 0.42, 0.43, 0.44 } ; 
        double cdspg[]  = {270., 110., 54., 51., 40., 35., 28.,
                           15., 8.5, 7.5, 6.0, 5.4, 4.9,
                           3.1, 1.9, 1.8, 1.5, 1.3, 1.1,
                           0.81, 0.6, 0.58, 0.56, 0.5, 0.49, 
                           0.40, 0.41, 0.415, 0.42, 0.43, 0.44,
                           0.44, 0.28, 0.255, 0.24, 0.24, 0.25, 
                           0.26, 0.27, 0.290, 0.33, 0.37, 0.40, 
                           0.41, 0.42, 0.420, 0.43, 0.44, 0.45 } ; 
        if (anflag == 0)
            {
            dragco = 0;
            }

        else if (anflag == 1)
            {
            switch (foil)
                {
                case 1:    //airfoil drag logic
                    {
                    dragCam0Thk5 = -9E-07*Math.pow(alfd,3) + 0.0007*Math.pow(alfd,2) + 0.0008*alfd + 0.015;
                    dragCam5Thk5 = 4E-08*Math.pow(alfd,5) - 7E-07*Math.pow(alfd,4) - 1E-05*Math.pow(alfd,3) + 0.0009*Math.pow(alfd,2) + 0.0033*alfd + 0.0301;
                    dragCam10Thk5 = -9E-09*Math.pow(alfd,6) - 6E-08*Math.pow(alfd,5) + 5E-06*Math.pow(alfd,4) + 3E-05*Math.pow(alfd,3) - 0.0001*Math.pow(alfd,2) - 0.0025*alfd + 0.0615;
                    dragCam15Thk5 = 8E-10*Math.pow(alfd,6) - 5E-08*Math.pow(alfd,5) - 1E-06*Math.pow(alfd,4) + 3E-05*Math.pow(alfd,3) + 0.0008*Math.pow(alfd,2) - 0.0027*alfd + 0.0612;
                    dragCam20Thk5 = 8E-9*Math.pow(alfd,6) + 1E-8*Math.pow(alfd,5) - 5E-6*Math.pow(alfd,4) + 6E-6*Math.pow(alfd,3) + 0.001*Math.pow(alfd,2) - 0.001*alfd + 0.1219;
                    
                    dragCam0Thk10 = -1E-08*Math.pow(alfd,6) + 6E-08*Math.pow(alfd,5) + 6E-06*Math.pow(alfd,4) - 2E-05*Math.pow(alfd,3) - 0.0002*Math.pow(alfd,2) + 0.0017*alfd + 0.0196;
                    dragCam5Thk10 = 3E-09*Math.pow(alfd,6) + 6E-08*Math.pow(alfd,5) - 2E-06*Math.pow(alfd,4) - 3E-05*Math.pow(alfd,3) + 0.0008*Math.pow(alfd,2) + 0.0038*alfd + 0.0159;
                    dragCam10Thk10 = -5E-09*Math.pow(alfd,6) - 3E-08*Math.pow(alfd,5) + 2E-06*Math.pow(alfd,4) + 1E-05*Math.pow(alfd,3) + 0.0004*Math.pow(alfd,2) - 3E-05*alfd + 0.0624;
                    dragCam15Thk10 = -2E-09*Math.pow(alfd,6) - 2E-08*Math.pow(alfd,5) - 5E-07*Math.pow(alfd,4) + 8E-06*Math.pow(alfd,3) + 0.0009*Math.pow(alfd,2) + 0.0034*alfd + 0.0993;
                    dragCam20Thk10 = 2E-09*Math.pow(alfd,6) - 3E-08*Math.pow(alfd,5) - 2E-06*Math.pow(alfd,4) + 2E-05*Math.pow(alfd,3) + 0.0009*Math.pow(alfd,2) + 0.0023*alfd + 0.1581;

                    dragCam0Thk15 = -5E-09*Math.pow(alfd,6) + 7E-08*Math.pow(alfd,5) + 3E-06*Math.pow(alfd,4) - 3E-05*Math.pow(alfd,3) - 7E-05*Math.pow(alfd,2) + 0.0017*alfd + 0.0358;
                    dragCam5Thk15 = -4E-09*Math.pow(alfd,6) - 8E-09*Math.pow(alfd,5) + 2E-06*Math.pow(alfd,4) - 9E-07*Math.pow(alfd,3) + 0.0002*Math.pow(alfd,2) + 0.0031*alfd + 0.0351;
                    dragCam10Thk15 = 3E-09*Math.pow(alfd,6) + 3E-08*Math.pow(alfd,5) - 2E-06*Math.pow(alfd,4) - 1E-05*Math.pow(alfd,3) + 0.0009*Math.pow(alfd,2) + 0.004*alfd + 0.0543;
                    dragCam15Thk15 = 3E-09*Math.pow(alfd,6) + 5E-08*Math.pow(alfd,5) - 2E-06*Math.pow(alfd,4) - 3E-05*Math.pow(alfd,3) + 0.0008*Math.pow(alfd,2) + 0.0087*alfd + 0.1167;
                    dragCam20Thk15 = 3E-10*Math.pow(alfd,6) - 3E-08*Math.pow(alfd,5) - 6E-07*Math.pow(alfd,4) + 3E-05*Math.pow(alfd,3) + 0.0006*Math.pow(alfd,2) + 0.0008*alfd + 0.1859;

                    dragCam0Thk20 = -3E-09*Math.pow(alfd,6) + 2E-08*Math.pow(alfd,5) + 2E-06*Math.pow(alfd,4) - 8E-06*Math.pow(alfd,3) - 4E-05*Math.pow(alfd,2) + 0.0003*alfd + 0.0416;
                    dragCam5Thk20 = -3E-09*Math.pow(alfd,6) - 7E-08*Math.pow(alfd,5) + 1E-06*Math.pow(alfd,4) + 3E-05*Math.pow(alfd,3) + 0.0004*Math.pow(alfd,2) + 5E-05*alfd + 0.0483;
                    dragCam10Thk20 = 1E-08*Math.pow(alfd,6) + 4E-08*Math.pow(alfd,5) - 6E-06*Math.pow(alfd,4) - 2E-05*Math.pow(alfd,3) + 0.0014*Math.pow(alfd,2) + 0.007*alfd + 0.0692;
                    dragCam15Thk20 = 3E-09*Math.pow(alfd,6) - 9E-08*Math.pow(alfd,5) - 3E-06*Math.pow(alfd,4) + 4E-05*Math.pow(alfd,3) + 0.001*Math.pow(alfd,2) + 0.0021*alfd + 0.139;
                    dragCam20Thk20 = 3E-09*Math.pow(alfd,6) - 7E-08*Math.pow(alfd,5) - 3E-06*Math.pow(alfd,4) + 4E-05*Math.pow(alfd,3) + 0.0012*Math.pow(alfd,2) + 0.001*alfd + 0.1856;

                    if (-20.0 <= camd && camd < -15.0)
                        {
                        dragThk5 = (camd/5 + 4)*(dragCam15Thk5 - dragCam20Thk5) + dragCam20Thk5;
                        dragThk10 = (camd/5 + 4)*(dragCam15Thk10 - dragCam20Thk10) + dragCam20Thk10;
                        dragThk15 = (camd/5 + 4)*(dragCam15Thk15 - dragCam20Thk15) + dragCam20Thk15;
                        dragThk20 = (camd/5 + 4)*(dragCam15Thk20 - dragCam20Thk20) + dragCam20Thk20;
                    
                        if (1.0 <= thkd && thkd <= 5.0)
                            {
                            dragco = dragThk5;
                            }
                        else if (5.0 < thkd && thkd <= 10.0)
                            {
                            dragco = (thkd/5 - 1)*(dragThk10 - dragThk5) + dragThk5;
                            }
                        else if (10.0 < thkd && thkd <= 15.0)
                            {
                            dragco = (thkd/5 - 2)*(dragThk15 - dragThk10) + dragThk10;
                            }
                        else if (15.0 < thkd && thkd <= 20.0)
                            {
                            dragco = (thkd/5 - 3)*(dragThk20 - dragThk15) + dragThk15;
                            }
                        }
                    else if (-15.0 <= camd && camd < -10.0)
                        {
                        dragThk5 = (camd/5 + 3)*(dragCam10Thk5 - dragCam15Thk5) + dragCam15Thk5;
                        dragThk10 = (camd/5 + 3)*(dragCam10Thk10 - dragCam15Thk10) + dragCam15Thk10;
                        dragThk15 = (camd/5 + 3)*(dragCam10Thk15 - dragCam15Thk15) + dragCam15Thk15;
                        dragThk20 = (camd/5 + 3)*(dragCam10Thk20 - dragCam15Thk20) + dragCam15Thk20;

                        if (1.0 <= thkd && thkd <= 5.0)
                            {
                            dragco = dragThk5;
                            }
                        else if (5.0 < thkd && thkd <= 10.0)
                            {
                            dragco = (thkd/5 - 1)*(dragThk10 - dragThk5) + dragThk5;
                            }
                        else if (10.0 < thkd && thkd <= 15.0)
                            {
                            dragco = (thkd/5 - 2)*(dragThk15 - dragThk10) + dragThk10;
                            }
                        else if (15.0 < thkd && thkd <= 20.0)
                            {
                            dragco = (thkd/5 - 3)*(dragThk20 - dragThk15) + dragThk15;
                            }
                        }
                    else if (-10.0 <= camd && camd < -5.0)
                        {
                        dragThk5 = (camd/5 + 2)*(dragCam5Thk5 - dragCam10Thk5) + dragCam10Thk5;
                        dragThk10 = (camd/5 + 2)*(dragCam5Thk10 - dragCam10Thk10) + dragCam10Thk10;
                        dragThk15 = (camd/5 + 2)*(dragCam5Thk15 - dragCam10Thk15) + dragCam10Thk15;
                        dragThk20 = (camd/5 + 2)*(dragCam5Thk20 - dragCam10Thk20) + dragCam10Thk20;

                        if (1.0 <= thkd && thkd <= 5.0)
                            {
                            dragco = dragThk5;
                            }
                        else if (5.0 < thkd && thkd <= 10.0)
                            {
                            dragco = (thkd/5 - 1)*(dragThk10 - dragThk5) + dragThk5;
                            }
                        else if (10.0 < thkd && thkd <= 15.0)
                            {
                            dragco = (thkd/5 - 2)*(dragThk15 - dragThk10) + dragThk10;
                            }
                        else if (15.0 < thkd && thkd <= 20.0)
                            {
                            dragco = (thkd/5 - 3)*(dragThk20 - dragThk15) + dragThk15;
                            }
                        }
                    else if (-5.0 <= camd && camd < 0)
                        {
                        dragThk5 = (camd/5 + 1)*(dragCam0Thk5 - dragCam5Thk5) + dragCam5Thk5;
                        dragThk10 = (camd/5 + 1)*(dragCam0Thk10 - dragCam5Thk10) + dragCam5Thk10;
                        dragThk15 = (camd/5 + 1)*(dragCam0Thk15 - dragCam5Thk15) + dragCam5Thk15;
                        dragThk20 = (camd/5 + 1)*(dragCam0Thk20 - dragCam5Thk20) + dragCam5Thk20;

                        if (1.0 <= thkd && thkd <= 5.0)
                            {
                            dragco = dragThk5;
                            }
                        else if (5.0 < thkd && thkd <= 10.0)
                            {
                            dragco = (thkd/5 - 1)*(dragThk10 - dragThk5) + dragThk5;
                            }
                        else if (10.0 < thkd && thkd <= 15.0)
                            {
                            dragco = (thkd/5 - 2)*(dragThk15 - dragThk10) + dragThk10;
                            }
                        else if (15.0 < thkd && thkd <= 20.0)
                            {
                            dragco = (thkd/5 - 3)*(dragThk20 - dragThk15) + dragThk15;
                            }
                        }
                    else if (0 <= camd && camd < 5)
                        {
                        dragThk5 = (camd/5)*(dragCam5Thk5 - dragCam0Thk5) + dragCam0Thk5;
                        dragThk10 = (camd/5)*(dragCam5Thk10 - dragCam0Thk10) + dragCam0Thk10;
                        dragThk15 = (camd/5)*(dragCam5Thk15 - dragCam0Thk15) + dragCam0Thk15;
                        dragThk20 = (camd/5)*(dragCam5Thk20 - dragCam0Thk20) + dragCam0Thk20;

                        if (1.0 <= thkd && thkd <= 5.0)
                            {
                            dragco = dragThk5;
                            }
                        else if (5.0 < thkd && thkd <= 10.0)
                            {
                            dragco = (thkd/5 - 1)*(dragThk10 - dragThk5) + dragThk5;
                            }
                        else if (10.0 < thkd && thkd <= 15.0)
                            {
                            dragco = (thkd/5 - 2)*(dragThk15 - dragThk10) + dragThk10;
                            }
                        else if (15.0 < thkd && thkd <= 20.0)
                            {
                            dragco = (thkd/5 - 3)*(dragThk20 - dragThk15) + dragThk15;
                            }
                        }
                    else if (5 <= camd && camd < 10)
                        {
                        dragThk5 = (camd/5 - 1)*(dragCam10Thk5 - dragCam5Thk5) + dragCam5Thk5;
                        dragThk10 = (camd/5 - 1)*(dragCam10Thk10 - dragCam5Thk10) + dragCam5Thk10;
                        dragThk15 = (camd/5 - 1)*(dragCam10Thk15 - dragCam5Thk15) + dragCam5Thk15;
                        dragThk20 = (camd/5 - 1)*(dragCam10Thk20 - dragCam5Thk20) + dragCam5Thk20;

                        if (1.0 <= thkd && thkd <= 5.0)
                            {
                            dragco = dragThk5;
                            }
                        else if (5.0 < thkd && thkd <= 10.0)
                            {
                            dragco = (thkd/5 - 1)*(dragThk10 - dragThk5) + dragThk5;
                            }
                        else if (10.0 < thkd && thkd <= 15.0)
                            {
                            dragco = (thkd/5 - 2)*(dragThk15 - dragThk10) + dragThk10;
                            }
                        else if (15.0 < thkd && thkd <= 20.0)
                            {
                            dragco = (thkd/5 - 3)*(dragThk20 - dragThk15) + dragThk15;
                            }
                        }
                    else if (10 <= camd && camd < 15)
                        {
                        dragThk5 = (camd/5 - 2)*(dragCam15Thk5 - dragCam10Thk5) + dragCam10Thk5;
                        dragThk10 = (camd/5 - 2)*(dragCam15Thk10 - dragCam10Thk10) + dragCam10Thk10;
                        dragThk15 = (camd/5 - 2)*(dragCam15Thk15 - dragCam10Thk15) + dragCam10Thk15;
                        dragThk20 = (camd/5 - 2)*(dragCam15Thk20 - dragCam10Thk20) + dragCam10Thk20;

                        if (1.0 <= thkd && thkd <= 5.0)
                            {
                            dragco = dragThk5;
                            }
                        else if (5.0 < thkd && thkd <= 10.0)
                            {
                            dragco = (thkd/5 - 1)*(dragThk10 - dragThk5) + dragThk5;
                            }
                        else if (10.0 < thkd && thkd <= 15.0)
                            {
                            dragco = (thkd/5 - 2)*(dragThk15 - dragThk10) + dragThk10;
                            }
                        else if (15.0 < thkd && thkd <= 20.0)
                            {
                            dragco = (thkd/5 - 3)*(dragThk20 - dragThk15) + dragThk15;
                            }
                        }
                    else if (15 <= camd && camd <= 20)
                        {
                        dragThk5 = (camd/5 - 3)*(dragCam20Thk5 - dragCam15Thk5) + dragCam15Thk5;
                        dragThk10 = (camd/5 - 3)*(dragCam20Thk10 - dragCam15Thk10) + dragCam15Thk10;
                        dragThk15 = (camd/5 - 3)*(dragCam20Thk15 - dragCam15Thk15) + dragCam15Thk15;
                        dragThk20 = (camd/5 - 3)*(dragCam20Thk20 - dragCam15Thk20) + dragCam15Thk20;

                        if (1.0 <= thkd && thkd <= 5.0)
                            {
                            dragco = dragThk5;
                            }
                        else if (5.0 < thkd && thkd <= 10.0)
                            {
                            dragco = (thkd/5 - 1)*(dragThk10 - dragThk5) + dragThk5;
                            }
                        else if (10.0 < thkd && thkd <= 15.0)
                            {
                            dragco = (thkd/5 - 2)*(dragThk15 - dragThk10) + dragThk10;
                            }
                        else if (15.0 < thkd && thkd <= 20.0)
                            {
                            dragco = (thkd/5 - 3)*(dragThk20 - dragThk15) + dragThk15;
                            }
                        }

                    break;
                    }
                case 2:   //elliptical drag logic
                    {
                    dragCam0Thk5 = -6E-07*Math.pow(alfd,3) + 0.0007*Math.pow(alfd,2) + 0.0007*alfd + 0.0428;
                    dragCam10Thk5 = 5E-09*Math.pow(alfd,6) - 7E-08*Math.pow(alfd,5) - 3E-06*Math.pow(alfd,4) + 5E-05*Math.pow(alfd,3) + 0.0009*Math.pow(alfd,2) - 0.0058*alfd + 0.0758;
                    dragCam20Thk5 = 1E-08*Math.pow(alfd,6) - 2E-08*Math.pow(alfd,5) - 7E-06*Math.pow(alfd,4) + 1E-05*Math.pow(alfd,3) + 0.0015*Math.pow(alfd,2) + 0.0007*alfd + 0.1594;
                    
                    dragCam0Thk10 = 3E-09*Math.pow(alfd,6) + 4E-08*Math.pow(alfd,5) - 3E-06*Math.pow(alfd,4) - 9E-06*Math.pow(alfd,3) + 0.0013*Math.pow(alfd,2) + 0.0007*alfd + 0.0112;
                    dragCam10Thk10 = -4E-09*Math.pow(alfd,6) - 9E-08*Math.pow(alfd,5) + 2E-06*Math.pow(alfd,4) + 7E-05*Math.pow(alfd,3) + 0.0008*Math.pow(alfd,2) - 0.0095*alfd + 0.0657;
                    dragCam20Thk10 = -8E-09*Math.pow(alfd,6) - 9E-08*Math.pow(alfd,5) + 3E-06*Math.pow(alfd,4) + 6E-05*Math.pow(alfd,3) + 0.0005*Math.pow(alfd,2) - 0.0088*alfd + 0.2088;

                    dragCam0Thk20 = -7E-09*Math.pow(alfd,6) - 1E-07*Math.pow(alfd,5) + 4E-06*Math.pow(alfd,4) + 6E-05*Math.pow(alfd,3) + 0.0001*Math.pow(alfd,2) - 0.0087*alfd + 0.0596;
                    dragCam10Thk20 = -2E-09*Math.pow(alfd,6) + 2E-07*Math.pow(alfd,5) + 1E-06*Math.pow(alfd,4) - 6E-05*Math.pow(alfd,3) + 0.0004*Math.pow(alfd,2) - 7E-05*alfd + 0.1114;
                    dragCam20Thk20 = 4E-09*Math.pow(alfd,6) - 7E-08*Math.pow(alfd,5) - 3E-06*Math.pow(alfd,4) + 3E-05*Math.pow(alfd,3) + 0.001*Math.pow(alfd,2) - 0.0018*alfd + 0.1925;

                    if (-20.0 <= camd && camd < -10.0)
                        {
                        dragThk5 = (camd/10 + 2)*(dragCam10Thk5 - dragCam20Thk5) + dragCam20Thk5;
                        dragThk10 = (camd/10 + 2)*(dragCam10Thk10 - dragCam20Thk10) + dragCam20Thk10;
                        dragThk20 = (camd/10 + 2)*(dragCam10Thk20 - dragCam20Thk20) + dragCam20Thk20;
                    
                        if (1.0 <= thkd && thkd <= 5.0)
                            {
                            dragco = dragThk5;
                            }
                        else if (5.0 < thkd && thkd <= 10.0)
                            {
                            dragco = (thkd/5 - 1)*(dragThk10 - dragThk5) + dragThk5;
                            }
                        else if (10.0 < thkd && thkd <= 20.0)
                            {
                            dragco = (thkd/10 - 1)*(dragThk20 - dragThk10) + dragThk10;
                            }
                        }
                    else if (-10.0 <= camd && camd < 0)
                        {
                        dragThk5 = (camd/10 + 1)*(dragCam0Thk5 - dragCam10Thk5) + dragCam10Thk5;
                        dragThk10 = (camd/10 + 1)*(dragCam0Thk10 - dragCam10Thk10) + dragCam10Thk10;
                        dragThk20 = (camd/10 + 1)*(dragCam0Thk20 - dragCam10Thk20) + dragCam10Thk20;

                        if (1.0 <= thkd && thkd <= 5.0)
                            {
                            dragco = dragThk5;
                            }
                        else if (5.0 < thkd && thkd <= 10.0)
                            {
                            dragco = (thkd/5 - 1)*(dragThk10 - dragThk5) + dragThk5;
                            }
                        else if (10.0 < thkd && thkd <= 20.0)
                            {
                            dragco = (thkd/10 - 1)*(dragThk20 - dragThk10) + dragThk10;
                            }
                        }
                    else if (0 <= camd && camd < 10)
                        {
                        dragThk5 = (camd/10)*(dragCam10Thk5 - dragCam0Thk5) + dragCam0Thk5;
                        dragThk10 = (camd/10)*(dragCam10Thk10 - dragCam0Thk10) + dragCam0Thk10;
                        dragThk20 = (camd/10)*(dragCam10Thk20 - dragCam0Thk20) + dragCam0Thk20;

                        if (1.0 <= thkd && thkd <= 5.0)
                            {
                            dragco = dragThk5;
                            }
                        else if (5.0 < thkd && thkd <= 10.0)
                            {
                            dragco = (thkd/5 - 1)*(dragThk10 - dragThk5) + dragThk5;
                            }
                        else if (10.0 < thkd && thkd <= 20.0)
                            {
                            dragco = (thkd/10 - 1)*(dragThk20 - dragThk10) + dragThk10;
                            }
                        }
                    else if (10 <= camd && camd < 20)
                        {
                        dragThk5 = (camd/10 - 1)*(dragCam20Thk5 - dragCam10Thk5) + dragCam10Thk5;
                        dragThk10 = (camd/10 - 1)*(dragCam20Thk10 - dragCam10Thk10) + dragCam10Thk10;
                        dragThk20 = (camd/10 - 1)*(dragCam20Thk20 - dragCam10Thk20) + dragCam10Thk20;

                        if (1.0 <= thkd && thkd <= 5.0)
                            {
                            dragco = dragThk5;
                            }
                        else if (5.0 < thkd && thkd <= 10.0)
                            {
                            dragco = (thkd/5 - 1)*(dragThk10 - dragThk5) + dragThk5;
                            }
                        else if (10.0 < thkd && thkd <= 20.0)
                            {
                            dragco = (thkd/10 - 1)*(dragThk20 - dragThk10) + dragThk10;
                            }
                        }

                    break;
                    }
                case 3:    //flat plate drag logic
                    {
                    dragCam0 = -9E-07*Math.pow(alfd,3) + 0.0007*Math.pow(alfd,2) + 0.0008*alfd + 0.015;
                    dragCam5 = 1E-08*Math.pow(alfd,6) + 4E-08*Math.pow(alfd,5) - 9E-06*Math.pow(alfd,4) - 1E-05*Math.pow(alfd,3) + 0.0021*Math.pow(alfd,2) + 0.0033*alfd + 0.006;
                    dragCam10 = -9E-09*Math.pow(alfd,6) - 6E-08*Math.pow(alfd,5) + 5E-06*Math.pow(alfd,4) + 3E-05*Math.pow(alfd,3) - 0.0001*Math.pow(alfd,2) - 0.0025*alfd + 0.0615;
                    dragCam15 = 8E-10*Math.pow(alfd,6) - 5E-08*Math.pow(alfd,5) - 1E-06*Math.pow(alfd,4) + 3E-05*Math.pow(alfd,3) + 0.0008*Math.pow(alfd,2) - 0.0027*alfd + 0.0612;
                    dragCam20 = 8E-9*Math.pow(alfd,6) + 1E-8*Math.pow(alfd,5) - 5E-6*Math.pow(alfd,4) + 6E-6*Math.pow(alfd,3) + 0.001*Math.pow(alfd,2) - 0.001*alfd + 0.1219;

                    if (-20.0 <= camd && camd < -15.0)
                        {
                        dragco = (camd/5 + 4)*(dragCam15 - dragCam20) + dragCam20;
                        }
                    else if (-15.0 <= camd && camd < -10.0)
                        {
                        dragco = (camd/5 + 3)*(dragCam10 - dragCam15) + dragCam15;
                        }
                    else if (-10.0 <= camd && camd < -5.0)
                        {
                        dragco = (camd/5 + 2)*(dragCam5 - dragCam10) + dragCam10;
                        }
                    else if (-5.0 <= camd && camd < 0)
                        {
                        dragco = (camd/5 + 1)*(dragCam0 - dragCam5) + dragCam5;
                        }
                    else if (0 <= camd && camd < 5)
                        {
                        dragco = (camd/5)*(dragCam5 - dragCam0) + dragCam0;
                        }
                    else if (5 <= camd && camd < 10)
                        {
                        dragco = (camd/5 - 1)*(dragCam10 - dragCam5) + dragCam5;
                        }
                    else if (10 <= camd && camd < 15)
                        {
                        dragco = (camd/5 - 2)*(dragCam15 - dragCam10) + dragCam10;
                        }
                    else if (15 <= camd && camd <= 20)
                        {
                        dragco = (camd/5 - 3)*(dragCam20 - dragCam15) + dragCam15;
                        }
                    break;
                    }
                case 4:   //cylinder drag logic
                    {
                    ifound = 0 ;
                    for (index = 0; index <= 43 ; ++ index) {
                        if(reynolds >= recyl[index] && reynolds < recyl[index+1]) ifound = index;
                    }

                    dragco = ((cdcyl[ifound+1]-cdcyl[ifound])/(recyl[ifound+1]-recyl[ifound]))*
                              (reynolds - recyl[ifound]) + cdcyl[ifound];

                    break;
                    }
                case 5:   //sphere drag logic
                    {
                    ifound = 0 ;
                    for (index = 0; index <= 48 ; ++ index) {
                        if(reynolds >= resps[index] && reynolds < resps[index+1]) ifound = index;
                    }
                    
                    if ( bdragflag == 1) {    // smooth ball
                       dragco = ((cdsps[ifound+1]-cdsps[ifound])/(resps[ifound+1]-resps[ifound]))*
                              (reynolds - resps[ifound]) + cdsps[ifound];
                    }
                    if ( bdragflag == 2) {    // rough ball
                       dragco = ((cdspr[ifound+1]-cdspr[ifound])/(resps[ifound+1]-resps[ifound]))*
                              (reynolds - resps[ifound]) + cdspr[ifound];
                    }
                    if ( bdragflag == 3) {    // golf ball
                       dragco = ((cdspg[ifound+1]-cdspg[ifound])/(resps[ifound+1]-resps[ifound]))*
                              (reynolds - resps[ifound]) + cdspg[ifound];
                    }

                    break;
                    }
                }

                if(recor == 1) {    // reynolds correction
                     if (foil <= 3) {       // airfoil 
                         dragco = dragco * Math.pow((50000./reynolds),.11) ;
                     }
                }
                if (indrag == 1) {    // induced drag coefficient  factor = .85 for rectangle
                    dragco = dragco + (cldin * cldin)/ (3.1415926 * aspr * .85) ;
                }
            }
        }
  } // end Solver

  class Con extends Panel {
     Foil outerparent ;
     Label l1,l2,blank,pitchMomentLabel,liftOverDrag,reynoldsLabel,FSlabel;
     Choice outch,dragOutputCh,untch;
     Button bt3,ibt1,ibt2,ibt3,ibt4,ibt5,ibt6 ;
     Button obt1,obt2,obt3,obt4,obt5 ;
     TextField outlft,outDrag,outMoment,outLD,outReynolds ;
   
     Con (Foil target) { 
       outerparent = target ;
       setLayout(new GridLayout(7,4,5,5)) ;

       l1 = new Label("Output", Label.RIGHT) ;
       l1.setForeground(Color.red) ;
       l2 = new Label("Input", Label.CENTER) ;
       l2.setForeground(Color.blue) ;

       FSlabel = new Label("FoilSim III", Label.CENTER) ;
       FSlabel.setForeground(Color.red) ;

       bt3 = new Button("Reset") ;
       bt3.setBackground(Color.red) ;
       bt3.setForeground(Color.white) ;

       ibt1 = new Button("Flight") ;
       ibt1.setBackground(Color.white) ;
       ibt1.setForeground(Color.blue) ;

       ibt2 = new Button("Shape") ;
       ibt2.setBackground(Color.yellow) ;
       ibt2.setForeground(Color.blue) ;

       ibt3 = new Button("Size") ;
       ibt3.setBackground(Color.white) ;
       ibt3.setForeground(Color.blue) ;

       ibt4 = new Button("Select Plot") ;
       ibt4.setBackground(Color.white) ;
       ibt4.setForeground(Color.blue) ;

       ibt5 = new Button("Analysis") ;
       ibt5.setBackground(Color.white) ;
       ibt5.setForeground(Color.blue) ;

       ibt6 = new Button("Generation") ;
       ibt6.setBackground(Color.white) ;
       ibt6.setForeground(Color.blue) ;

       obt1 = new Button("Plot") ;
       obt1.setBackground(Color.yellow) ;
       obt1.setForeground(Color.red) ;

       obt2 = new Button("Probe") ;
       obt2.setBackground(Color.white) ;
       obt2.setForeground(Color.red) ;

       obt3 = new Button("Gages") ;
       obt3.setBackground(Color.white) ;
       obt3.setForeground(Color.red) ;

       obt4 = new Button("Geometry") ;
       obt4.setBackground(Color.white) ;
       obt4.setForeground(Color.red) ;

       obt5 = new Button("Data") ;
       obt5.setBackground(Color.white) ;
       obt5.setForeground(Color.red) ;

       outch = new Choice() ;
       outch.setBackground(Color.white) ;
       outch.setForeground(Color.black) ;
       outch.addItem("Lift ") ;
       outch.addItem("  Cl ");
       outch.select(0) ;

       untch = new Choice() ;
       untch.setBackground(Color.white) ;
       untch.setForeground(Color.black) ;
       untch.addItem("Imperial ") ;
       untch.addItem("Metric ");
       untch.select(0) ;

       dragOutputCh = new Choice();
       dragOutputCh.setBackground(Color.white);
       dragOutputCh.setForeground(Color.black);
       dragOutputCh.addItem("Drag");
       dragOutputCh.addItem(" Cd ");
       dragOutputCh.select(0);

       outlft = new TextField("12.5",5) ;
       outlft.setBackground(Color.black) ;
       outlft.setForeground(Color.yellow) ;

       outDrag = new TextField("12.5",5);
       outDrag.setBackground(Color.black);
       outDrag.setForeground(Color.yellow);

       pitchMomentLabel = new Label("Cm",Label.RIGHT);
 
       liftOverDrag = new Label("L/D ratio",Label.RIGHT);
       liftOverDrag.setForeground(Color.black);

       outLD = new TextField("12.5",5);
       outLD.setBackground(Color.black);
       outLD.setForeground(Color.yellow);

       outReynolds = new TextField("12.5",5);
       outReynolds.setBackground(Color.black);
       outReynolds.setForeground(Color.yellow);

       reynoldsLabel = new Label("Reynolds #", Label.RIGHT);
       reynoldsLabel.setForeground(Color.black);

       add(FSlabel) ;
       add(new Label(" Units:", Label.RIGHT));
       add(untch);
       add(bt3) ;

       add(l2) ;
       add(new Label("Undergrad ", Label.RIGHT));
       add(new Label(" Version 1.4d", Label.LEFT));
       add(l1) ;

       add(ibt1);
       add(ibt2);
       add(obt2);
       add(obt3);

       add(ibt3);
       add(ibt5);
       add(obt4) ;
       add(obt5);

       add(ibt6);
       add(ibt4);
       add(obt1);
       add(new Label(" ", Label.CENTER));

       add(outch) ;
       add(outlft) ;
       add(reynoldsLabel);
       add(outReynolds);

       add(dragOutputCh);
       add(outDrag);
       add(liftOverDrag);
       add(outLD);
     }

     public boolean action(Event evt, Object arg) {

       if(evt.target instanceof Button) {
          handleBut (evt,arg) ;
          return true ;
       }
       if(evt.target instanceof Choice) {
          lftout = outch.getSelectedIndex() ;
          dragOut = dragOutputCh.getSelectedIndex();
          lunits = untch.getSelectedIndex() ;

          setUnits() ;
          loadInput() ;

          return true ;
       }
       else return false ;
     } // Handler
 
      public void handleBut(Event evt, Object arg) {
        float fl1,fl2,fl3 ;
        int i1,i2,i3 ;
        int index ;
        double mapfac,volume ; 

        String label = (String)arg ;

        if(label.equals("Reset")) {
           solve.setDefaults() ;
           in.flt.inr.plntch.select(0) ;
           in.shp.inr.shapch.select(0);
           in.cyl.inr.shapch.select(0);
           in.shp.inl.inl1.setBackground(Color.yellow) ;
           in.shp.inl.inl2.setBackground(Color.white) ;
           in.shp.inl.inl3.setBackground(Color.white) ;
           in.shp.inr.inr1.inb2.setBackground(Color.white) ;
           in.shp.inr.inr1.inb1.setBackground(Color.white) ;
           in.shp.inr.inr2.inb3.setBackground(Color.white) ;
           in.shp.inr.inr2.inb4.setBackground(Color.white) ;

           in.flt.inl.o1.setBackground(Color.black) ;
           in.flt.inl.o1.setForeground(Color.yellow) ;
           in.flt.inr.inr2.o2.setBackground(Color.black) ;
           in.flt.inr.inr2.o2.setForeground(Color.yellow) ;
           in.flt.inl.o3.setBackground(Color.black) ;
           in.flt.inl.o3.setForeground(Color.yellow) ;
           in.flt.inr.inr3.o4.setBackground(Color.black) ;
           in.flt.inr.inr3.o4.setForeground(Color.yellow) ;
           layin.show(in, "second")  ;
           ibt2.setBackground(Color.yellow) ;
           ibt1.setBackground(Color.white) ;
           ibt3.setBackground(Color.white) ;
           ibt4.setBackground(Color.white) ;
           ibt5.setBackground(Color.white) ;
           ibt6.setBackground(Color.white) ;
 //          in.anl.bt1.setBackground(Color.yellow) ;
 //          in.anl.bt2.setBackground(Color.white) ;
           in.anl.bt3.setBackground(Color.white) ;
           in.anl.bt4.setBackground(Color.yellow) ;
           in.anl.bt5.setBackground(Color.yellow) ;
           in.anl.bt6.setBackground(Color.white) ;
           in.anl.bt7.setBackground(Color.yellow) ;
           in.anl.bt8.setBackground(Color.white) ;
           in.anl.bt9.setBackground(Color.yellow) ;
           in.anl.bt10.setBackground(Color.white) ;
           in.anl.bt11.setBackground(Color.yellow) ;
           in.anl.bt12.setBackground(Color.white) ;

           in.anl.cbt1.setBackground(Color.white) ;
           in.anl.cbt2.setBackground(Color.white) ; 
           in.anl.cbt3.setBackground(Color.white) ; 

                // **** the lunits check MUST come first
           untch.select(0) ;
           setUnits () ;
           lftout = outch.getSelectedIndex() ;
           dragOut = dragOutputCh.getSelectedIndex();
           layplt.show(in.grf.l, "first") ;
           layout.show(out, "first")  ;
           obt1.setBackground(Color.yellow) ;
           obt2.setBackground(Color.white) ;
           obt3.setBackground(Color.white) ;
           obt4.setBackground(Color.white) ;
           obt5.setBackground(Color.white) ;
           out.prb.l.bt3.setBackground(Color.white) ;
           out.prb.l.bt2.setBackground(Color.white) ;
           out.prb.l.bt1.setBackground(Color.white) ;
           outopt = 0 ;
        }

        if(label.equals("Flight")) {
           layin.show(in, "first")  ;
           ibt1.setBackground(Color.yellow) ;
           ibt2.setBackground(Color.white) ;
           ibt3.setBackground(Color.white) ;
           ibt4.setBackground(Color.white) ;
           ibt5.setBackground(Color.white) ;
           ibt6.setBackground(Color.white) ;
           flflag = 1 ;
           probflag = 2 ;
        } 

        if(label.equals("Shape")) {
           if (foil <= 3) layin.show(in, "second")  ;
           if (foil >= 4) layin.show(in, "fifth")  ;
           ibt1.setBackground(Color.white) ;
           ibt2.setBackground(Color.yellow) ;
           ibt3.setBackground(Color.white) ;
           ibt4.setBackground(Color.white) ;
           ibt5.setBackground(Color.white) ;
           ibt6.setBackground(Color.white) ;
           flflag = 1 ;
           probflag = 0 ;
        } 

        if(label.equals("Size")) {
           if (foil <= 3) layin.show(in, "third")  ;
           if (foil >= 4) layin.show(in, "fifth")  ;
           ibt1.setBackground(Color.white) ;
           ibt2.setBackground(Color.white) ;
           ibt3.setBackground(Color.yellow) ;
           ibt4.setBackground(Color.white) ;
           ibt5.setBackground(Color.white) ;
           ibt6.setBackground(Color.white) ;
           flflag = 1 ;
           probflag = 1 ;
        } 

        if(label.equals("Select Plot")) {
           layin.show(in, "fourth")  ;
           ibt1.setBackground(Color.white) ;
           ibt2.setBackground(Color.white) ;
           ibt3.setBackground(Color.white) ;
           ibt4.setBackground(Color.yellow) ;
           ibt5.setBackground(Color.white) ;
           ibt6.setBackground(Color.white) ;
           flflag = 1;
           pboflag = 0 ;
        } 

        if(label.equals("Analysis")) {
           layin.show(in, "sixth")  ;
           ibt1.setBackground(Color.white) ;
           ibt2.setBackground(Color.white) ;
           ibt3.setBackground(Color.white) ;
           ibt4.setBackground(Color.white) ;
           ibt5.setBackground(Color.yellow) ;
           ibt6.setBackground(Color.white) ;
        } 

        if(label.equals("Generation")) {
           layin.show(in, "seventh")  ;
           ibt1.setBackground(Color.white) ;
           ibt2.setBackground(Color.white) ;
           ibt3.setBackground(Color.white) ;
           ibt4.setBackground(Color.white) ;
           ibt5.setBackground(Color.white) ;
           ibt6.setBackground(Color.yellow) ;
           dispp = 25 ;
           probflag = 3 ;
           flflag = 0 ;
        } 

        if(label.equals("Geometry")) {
           obt1.setBackground(Color.white) ;
           obt2.setBackground(Color.white) ;
           obt3.setBackground(Color.white) ;
           obt4.setBackground(Color.yellow) ;
           obt5.setBackground(Color.white) ;

           layout.show(out, "third")  ;
           pboflag = 0 ;
           volume = 0.0 ;

           switch(foil) {
             case 1: {       
                out.perf.prnt.appendText( "\n\n Joukowski Airfoil" ) ;
                break ;
             }
             case 2: {        
                out.perf.prnt.appendText( "\n\n Elliptical Airfoil" ) ;
                break ;
             }
             case 3: {     
                out.perf.prnt.appendText( "\n\n Plate" ) ;
                break ;
             }
             case 4: {     
                out.perf.prnt.appendText( "\n\n Rotating Cylinder" ) ;
                break ;
             }
             case 5: {     
                out.perf.prnt.appendText( "\n\n Spinning Ball" ) ;
                break ;
             }
           }
           if (foil <= 3) {
             out.perf.prnt.appendText( "\n Camber = " + filter3(caminpt) ) ;
             out.perf.prnt.appendText( " % chord ," ) ;
             out.perf.prnt.appendText( " Thickness = " + filter3(thkinpt) ) ;
             out.perf.prnt.appendText( " % chord ," ) ;
             out.perf.prnt.appendText( "\n Chord = " + filter3(chord) ) ;
             if (lunits == 0) out.perf.prnt.appendText( " ft ," ) ;
             if (lunits == 1) out.perf.prnt.appendText( " m ," ) ;
             out.perf.prnt.appendText( " Span = " + filter3(span) ) ;
             if (lunits == 0) out.perf.prnt.appendText( " ft ," ) ;
             if (lunits == 1) out.perf.prnt.appendText( " m ," ) ;
             out.perf.prnt.appendText( "\n Angle of attack = " + filter3(alfval) );
             out.perf.prnt.appendText( " degrees ," ) ;
           }
           if (foil >= 4) {
              out.perf.prnt.appendText( "\n Spin  = " + filter3(spin*60.0) ) ;
              out.perf.prnt.appendText( " rpm ," ) ;
              out.perf.prnt.appendText( " Radius = " + filter3(radius) ) ;
              if (lunits == 0) out.perf.prnt.appendText( " ft ," ) ;
              if (lunits == 1) out.perf.prnt.appendText( " m ," ) ;
           }
           switch(planet) {
             case 0: {       
                out.perf.prnt.appendText( "\n Standard Earth Atmosphere" ) ;
                break ;
             }
             case 1: {       
                out.perf.prnt.appendText( "\n Martian Atmosphere" ) ;
                break ;
             }
             case 2: {       
                out.perf.prnt.appendText( "\n Under Water" ) ;
                break ;
             }
             case 3: {       
                out.perf.prnt.appendText( "\n Specified Conditions" ) ;
                break ;
             }
             case 4: {       
                out.perf.prnt.appendText( "\n Specified Conditions" ) ;
                break ;
             }
           }
           switch (lunits)  {
              case 0: {                             /* English */
                 out.perf.prnt.appendText( "\n Ambient Pressure = " + filter3(ps0/144.) ) ;
                 out.perf.prnt.appendText( "lb/sq in," ) ;
                 break;
              }
              case 1: {                             /* Metric */
                 out.perf.prnt.appendText( "\n Ambient Pressure = " + filter3(101.3/14.7*ps0/144.) );
                 out.perf.prnt.appendText( "kPa," ) ;
                 break;
              }
           }
           out.perf.prnt.appendText( "\n Ambient Velocity = " + filter0(vfsd) ) ;
           if (lunits == 0) out.perf.prnt.appendText( " mph ," ) ;
           if (lunits == 1) out.perf.prnt.appendText( " km/hr ," ) ;

           out.perf.prnt.appendText( "\n \t Upper Surface \t \t \t ") ;
           out.perf.prnt.appendText( "\n X/c \t Y/c \t P \t V \t ");
           mapfac = 4.0 ;
           if (foil >= 4) mapfac = 2.0 ;
           for (index = 0; index <= npt2-1; ++ index) {
              out.perf.prnt.appendText( "\n" + filter3(xpl[0][npt2-index]/mapfac) +  "\t"
                + filter3(ypl[0][npt2-index]/mapfac) + "\t" + filter3(plp[npt2-index]) + "\t"
                + filter0(plv[npt2-index]) + "\t"); 
              if (index <= npt2-2) {
                 volume = volume + .5 * (((xpl[0][npt2-index-1]-xpl[0][npt2-index])
                    *(ypl[0][npt2-index]+ ypl[0][npt2-index-1])) / (mapfac * mapfac)) * chord ;
              }
           }
           out.perf.prnt.appendText( "\n \t Lower Surface \t \t \t ") ;
           for (index = 0; index <= npt2-1; ++ index) {
              out.perf.prnt.appendText( "\n"  + filter3(xpl[0][npt2+index]/mapfac) + "\t"
                + filter3(ypl[0][npt2+index]/mapfac) + "\t" + filter3(plp[npt2+index]) + "\t"
                + filter0(plv[npt2+index]) ) ;
              if (index <= npt2-2) {
                 volume = volume - .5 * (((xpl[0][npt2+index+1]-xpl[0][npt2+index])
                    *(ypl[0][npt2+index]+ ypl[0][npt2+index+1])) / (mapfac * mapfac)) * chord ;
              }
           }
           volume = volume * span ;
           if (foil >= 4) volume = 3.14159 * radius * radius * span ;
           if (foil >= 5) volume = 3.14159 * radius * radius * radius * 4.0 / 3.0 ;
           
           out.perf.prnt.appendText( "\n  Volume =" + filter3(volume));
           if (lunits == 0) out.perf.prnt.appendText( " cu ft " ) ;
           if (lunits == 1) out.perf.prnt.appendText( " cu m " ) ;
        }

        if(label.equals("Data")) {
           obt1.setBackground(Color.white) ;
           obt2.setBackground(Color.white) ;
           obt3.setBackground(Color.white) ;
           obt4.setBackground(Color.white) ;
           obt5.setBackground(Color.yellow) ;

           layout.show(out, "third")  ;
           pboflag = 0 ;

           switch(foil) {
             case 1: {       
                out.perf.prnt.appendText( "\n\n Joukowski Airfoil" ) ;
                break ;
             }
             case 2: {        
                out.perf.prnt.appendText( "\n\n Elliptical Airfoil" ) ;
                break ;
             }
             case 3: {     
                out.perf.prnt.appendText( "\n\n Plate" ) ;
                break ;
             }
             case 4: {     
                out.perf.prnt.appendText( "\n\n Rotating Cylinder" ) ;
                break ;
             }
             case 5: {     
                out.perf.prnt.appendText( "\n\n Spinning Ball" ) ;
                break ;
             }
           }
           if (foil <= 3) {
               out.perf.prnt.appendText( "\n Camber = " + filter3(caminpt) ) ;
               out.perf.prnt.appendText( " % chord ," ) ;
               out.perf.prnt.appendText( " Thickness = " + filter3(thkinpt) ) ;
               out.perf.prnt.appendText( " % chord ," ) ;
               out.perf.prnt.appendText( "\n Chord = " + filter3(chord) ) ;
               if (lunits == 0) out.perf.prnt.appendText( " ft ," ) ;
               if (lunits == 1) out.perf.prnt.appendText( " m ," ) ;
               out.perf.prnt.appendText( " Span = " + filter3(span) ) ;
               if (lunits == 0) out.perf.prnt.appendText( " ft ," ) ;
               if (lunits == 1) out.perf.prnt.appendText( " m ," ) ;
               out.perf.prnt.appendText( "\n Surface Area = " + filter3(area) ) ;
               if (lunits == 0) out.perf.prnt.appendText( " sq ft ," ) ;
               if (lunits == 1) out.perf.prnt.appendText( " sq m ," ) ;
               out.perf.prnt.appendText( "\n  Angle of attack = " + filter3(alfval) ) ;
               out.perf.prnt.appendText( " degrees ," ) ;
            }
            if (foil >= 4) {
               out.perf.prnt.appendText( "\n Spin  = " + filter3(spin*60.0) ) ;
               out.perf.prnt.appendText( " rpm ," ) ;
               out.perf.prnt.appendText( " Radius = " + filter3(radius) ) ;
               if (lunits == 0) out.perf.prnt.appendText( " ft ," ) ;
               if (lunits == 1) out.perf.prnt.appendText( " m ," ) ;
               out.perf.prnt.appendText( "\n Span = " + filter3(span) ) ;
               if (lunits == 0) out.perf.prnt.appendText( " ft ," ) ;
               if (lunits == 1) out.perf.prnt.appendText( " m ," ) ;
            }
            switch(planet) {
              case 0: {       
                 out.perf.prnt.appendText( "\n Standard Earth Atmosphere" ) ;
                 break ;
              }
              case 1: {       
                 out.perf.prnt.appendText( "\n Martian Atmosphere" ) ;
                 break ;
              }
              case 2: {       
                 out.perf.prnt.appendText( "\n Under Water" ) ;
                 break ;
              }
              case 3: {       
                 out.perf.prnt.appendText( "\n Specified Conditions" ) ;
                 break ;
              }
              case 4: {       
                 out.perf.prnt.appendText( "\n Specified Conditions" ) ;
                  break ;
              }
           }            out.perf.prnt.appendText( "\n Altitude = " + filter0(alt) ) ;
           if (lunits == 0) out.perf.prnt.appendText( " ft ," ) ;
           if (lunits == 1) out.perf.prnt.appendText( " m ," ) ;
           switch (lunits)  {
              case 0: {                             /* English */
                 out.perf.prnt.appendText( " Density = " + filter5(rho) ) ;
                 out.perf.prnt.appendText( "slug/cu ft" ) ;
                 out.perf.prnt.appendText( "\n Pressure = " + filter3(ps0/144.) ) ;
                 out.perf.prnt.appendText( "lb/sq in," ) ;
                 out.perf.prnt.appendText( " Temperature = " + filter0(ts0 - 460.) );
                 out.perf.prnt.appendText( "F," ) ;
                 break;
              }
              case 1: {                             /* Metric */
                 out.perf.prnt.appendText( " Density = " + filter3(rho*515.4) );
                 out.perf.prnt.appendText( "kg/cu m" ) ;
                 out.perf.prnt.appendText( "\n Pressure = " + filter3(101.3/14.7*ps0/144.) );
                 out.perf.prnt.appendText( "kPa," ) ;
                 out.perf.prnt.appendText( " Temperature = " + filter0(ts0*5.0/9.0 - 273.1) ) ;
                 out.perf.prnt.appendText( "C," ) ;
                 break;
              }
           }
           out.perf.prnt.appendText( "\n Airspeed = " + filter0(vfsd) ) ;
           if (lunits == 0) out.perf.prnt.appendText( " mph ," ) ;
           if (lunits == 1) out.perf.prnt.appendText( " km/hr ," ) ;
           if (lftout == 1)
              out.perf.prnt.appendText( "\n  Lift Coefficient = " + filter3(clift) ) ;
           if (lftout == 0) {
             if (Math.abs(lift) <= 10.0) out.perf.prnt.appendText( "\n  Lift = " + filter3(lift) ) ;
             if (Math.abs(lift) > 10.0) out.perf.prnt.appendText( "\n  Lift  = " + filter0(lift) ) ;
             if (lunits == 0) out.perf.prnt.appendText( " lbs " ) ;
             if (lunits == 1) out.perf.prnt.appendText( " Newtons " ) ;
           }
           if (dragOut == 1)
              out.perf.prnt.appendText( "\n  Drag Coefficient = " + filter3(dragCoeff) ) ;
           if (lftout == 0) {
              out.perf.prnt.appendText( "\n  Drag  = " + filter0(drag) ) ;
              if (lunits == 0) out.perf.prnt.appendText( " lbs " ) ;
              if (lunits == 1) out.perf.prnt.appendText( " Newtons " ) ;
           }
        }

        if(label.equals("Plot")) {
           obt1.setBackground(Color.yellow) ;
           obt2.setBackground(Color.white) ;
           obt3.setBackground(Color.white) ;
           obt4.setBackground(Color.white) ;
           obt5.setBackground(Color.white) ;
           dispp = 0;
           layout.show(out, "first")  ;
           pboflag = 0 ;
        }

        if(label.equals("Probe")) {
           obt1.setBackground(Color.white) ;
           obt2.setBackground(Color.yellow) ;
           obt3.setBackground(Color.white) ;
           obt4.setBackground(Color.white) ;
           obt5.setBackground(Color.white) ;
           layout.show(out, "second")  ;
           pboflag = 0 ;
        }

        if(label.equals("Gages")) {
           obt1.setBackground(Color.white) ;
           obt2.setBackground(Color.white) ;
           obt3.setBackground(Color.yellow) ;
           obt4.setBackground(Color.white) ;
           obt5.setBackground(Color.white) ;
           dispp = 20;
           layout.show(out, "first")  ;
           pboflag = 0 ;
        }

        loadInput() ;
     } //  handle buttons                  

  } // Con

  class In extends Panel {
     Foil outerparent ;
     Flt flt ;
     Shp shp ;
     Siz siz ;
     Cyl cyl ;
     Grf grf ;
     Anl anl ;
     Genp genp ;

     In (Foil target) { 
        outerparent = target ;
        layin = new CardLayout() ;
        setLayout(layin) ;

        flt = new Flt(outerparent) ;
        shp = new Shp(outerparent) ;
        siz = new Siz(outerparent) ;       
        cyl = new Cyl(outerparent) ;
        grf = new Grf(outerparent) ;
        anl = new Anl(outerparent) ;
        genp = new Genp(outerparent) ;

        add ("second", shp) ;
        add ("first", flt) ;
        add ("third", siz) ;
        add ("fifth", cyl) ;
        add ("fourth", grf) ;
        add ("sixth", anl) ;
        add ("seventh", genp) ;
     }
 
     class Flt extends Panel {
        Foil outerparent ;
        Inl inl ;
        Inr inr ;

        Flt (Foil target) {

           outerparent = target ;
           setLayout(new GridLayout(1,2,5,5)) ;

           inl = new Inl(outerparent) ;
           inr = new Inr(outerparent) ;

           add(inl) ;
           add(inr) ;
        }

        class Inl extends Panel {
           Foil outerparent ;
           TextField f1,f2,o1,o3 ;
           Label l1,l2 ;
           Label la1,la2 ;
           Label lo1,lo3 ;
     
           Inl (Foil target) {
    
            outerparent = target ;
            setLayout(new GridLayout(5,2,2,10)) ;

            la1 = new Label("Flight", Label.RIGHT) ;
            la1.setForeground(Color.blue) ;
            la2 = new Label("Test", Label.LEFT) ;
            la2.setForeground(Color.blue) ;

            l1 = new Label("Speed mph", Label.CENTER) ;
            f1 = new TextField("100.0",5) ;

            l2 = new Label("Altitude ft", Label.CENTER) ;
            f2 = new TextField("0.0",5) ;

            lo1 = new Label("Press lb/in^2", Label.CENTER) ;
            o1 = new TextField("0.0",5) ;
            o1.setBackground(Color.black) ;
            o1.setForeground(Color.yellow) ;

            lo3 = new Label("Dens slug/ft^3", Label.CENTER) ;
            o3 = new TextField("0.00027",5) ;
            o3.setBackground(Color.black) ;
            o3.setForeground(Color.yellow) ;

            add(la1) ;
            add(la2) ;

            add(l1) ;
            add(f1) ;

            add(l2) ;
            add(f2) ;

            add(lo1) ;
            add(o1) ;

            add(lo3) ;
            add(o3) ;
          }

          public boolean handleEvent(Event evt) {
            Double V1,V2,V3 ;
            double v1,v2,v3 ;
            float fl1 ;
            int i1,i2,i3 ;

            if(evt.id == Event.ACTION_EVENT) {
              V1 = Double.valueOf(f1.getText()) ;
              v1 = V1.doubleValue() ;
              V2 = Double.valueOf(f2.getText()) ;
              v2 = V2.doubleValue() ;

              vfsd = v1 ;
              if(v1 < vmn) {
                vfsd = v1 = vmn ;
                fl1 = (float) v1 ;
                f1.setText(String.valueOf(fl1)) ;
              }
              if(v1 > vmx) {
                vfsd = v1 = vmx ;
                fl1 = (float) v1 ;
                f1.setText(String.valueOf(fl1)) ;
              }

              alt = v2 ;
              if(v2 < almn) {
                alt = v2 = almn ;
                fl1 = (float) v2 ;
                f2.setText(String.valueOf(fl1)) ;
              }
              if(v2 > almx) {
                alt = v2 = almx ;
                fl1 = (float) v2 ;
                f2.setText(String.valueOf(fl1)) ;
              }
    
              i1 = (int) (((v1 - vmn)/(vmx-vmn))*1000.) ;
              i2 = (int) (((v2 - almn)/(almx-almn))*1000.) ;
     
              inr.s1.setValue(i1) ;
              inr.s2.setValue(i2) ;

              if (planet == 3) {    // read in the pressure
                V1 = Double.valueOf(o1.getText()) ;
                v1 = V1.doubleValue() ;
                ps0 = v1 /pconv * 2116. ;
                if(ps0 < .5) {
                  ps0 = .5 ;
                  v1 = ps0 / 2116. * pconv ;
                  fl1 = (float) v1 ;
                  o1.setText(String.valueOf(fl1)) ;
                }
                if(ps0 > 5000.) {
                  ps0 = 5000. ;
                  v1 = ps0 / 2116. * pconv ;
                  fl1 = (float) v1 ;
                  o1.setText(String.valueOf(fl1)) ;
                }
              }

              if (planet == 4) {    // read in the density
                   V1 = Double.valueOf(o3.getText()) ;
                   v1 = V1.doubleValue() ;
                   rho = v1 ;
                   if (lunits == 1) rho = v1 /515.4 ;
                   if(rho < .000001) {
                     rho = .000001 ;
                     v1 = rho;
                     if (lunits == 1) v1 = rho * 515.4 ;
                     fl1 = (float) v1 ;
                     o3.setText(String.valueOf(fl1)) ;
                   }
                   if(rho > 3.0) {
                     rho = 3. ;
                     v1 = rho;
                     if (lunits == 1) v1 = rho * 515.4 ;
                     fl1 = (float) v1 ;
                     o3.setText(String.valueOf(fl1)) ;
                   }
               }

       //  set limits on spin
              if(foil >= 4) cyl.setLims() ;

              computeFlow() ;
              return true ;
            }
            else return false ;
         } // Handler
       }  // Inl

        class Inr extends Panel {
           Foil outerparent ;
           Scrollbar s1,s2;
           Choice plntch;
           Inr2 inr2 ;
           Inr3 inr3 ;

           Inr (Foil target) {
            int i1,i2 ;

            outerparent = target ;
            setLayout(new GridLayout(5,1,2,10)) ;

            i1 = (int) (((100.0 - vmn)/(vmx-vmn))*1000.) ;
            i2 = (int) (((0.0 - almn)/(almx-almn))*1000.) ;

            plntch = new Choice() ;
            plntch.addItem("Earth - Average Day") ;
            plntch.addItem("Mars - Average Day");
            plntch.addItem("Water-Const Density");
            plntch.addItem("Specify Air T & P");
            plntch.addItem("Specify Density or Viscosity");
            plntch.setBackground(Color.white) ;
            plntch.setForeground(Color.blue) ;
            plntch.select(0) ;

            s1 = new Scrollbar(Scrollbar.HORIZONTAL,i1,10,0,1000);
            s2 = new Scrollbar(Scrollbar.HORIZONTAL,i2,10,0,1000);
            inr2 = new Inr2(outerparent) ;
            inr3 = new Inr3(outerparent) ;

            add(plntch) ;
            add(s1) ;
            add(s2) ;
            add(inr2) ;
            add(inr3) ;
          }

          class Inr3 extends Panel {
             Foil outerparent ;
             TextField o4;
             Label lo4 ;

             Inr3 (Foil target) {
               outerparent = target ;
               setLayout(new GridLayout(1,2,2,10)) ;

               lo4 = new Label("Visc slug/ft-s", Label.LEFT) ;
               o4 = new TextField("0.0",5) ;
               o4.setBackground(Color.black) ;
               o4.setForeground(Color.yellow) ;

               add(lo4) ;
               add(o4) ;
             }

             public boolean handleEvent(Event evt) {
               Double V1 ;
               double v1 ;
               float fl1 ;
  
               if(evt.id == Event.ACTION_EVENT) {

                if (planet == 4) {    // read in viscosity
                   V1 = Double.valueOf(o4.getText()) ;
                   v1 = V1.doubleValue() ;
                   viscos = v1 ;
                   if (lunits == 1) viscos = v1 /47.87 ;
                   if(viscos < .0000001) {
                     viscos = .0000001 ;
                     v1 = viscos;
                     if (lunits == 1) v1 = viscos * 47.87 ;
                     fl1 = (float) v1 ;
                     o4.setText(String.valueOf(fl1)) ;
                   }
                   if(viscos > 3.0) {
                     viscos = 3. ;
                     v1 = viscos;
                     if (lunits == 1) v1 = viscos * 47.87 ;
                     fl1 = (float) v1 ;
                     o4.setText(String.valueOf(fl1)) ;
                   }
                 }

                 computeFlow() ;
                 return true ;
               }
               else return false ;
             }
          }

          class Inr2 extends Panel {
             Foil outerparent ;
             TextField o2;
             Label lo2 ;

             Inr2 (Foil target) {
               outerparent = target ;
               setLayout(new GridLayout(1,2,2,10)) ;

               lo2 = new Label("Temp-F", Label.CENTER) ;
               o2 = new TextField("12.5",5) ;
               o2.setBackground(Color.black) ;
               o2.setForeground(Color.yellow) ;

               add(lo2) ;
               add(o2) ;
             }

             public boolean handleEvent(Event evt) {
               Double V1 ;
               double v1 ;
               float fl1 ;

               if(evt.id == Event.ACTION_EVENT) {

                 if (planet == 3) {    // read in the temperature
                   V1 = Double.valueOf(o2.getText()) ;
                   v1 = V1.doubleValue() ;
                   ts0 = v1 + 460. ;
                   if (lunits == 1) ts0 = (v1 + 273.1)*9.0/5.0 ;
                   if(ts0 < 350.) {
                     ts0 = 350. ;
                     v1 = ts0 - 460. ;
                     if (lunits == 1) v1 = ts0*5.0/9.0 - 273.1 ;
                     fl1 = (float) v1 ;
                     o2.setText(String.valueOf(fl1)) ;
                   }
                   if(ts0 > 660.) {
                     ts0 = 660. ;
                     v1 = ts0 - 460. ;
                     if (lunits == 1) v1 = ts0*5.0/9.0 - 273.1 ;
                     fl1 = (float) v1 ;
                     o2.setText(String.valueOf(fl1)) ;
                   }
                 }

                 computeFlow() ;
                 return true ;
               }
               else return false ;
             }
          }

          public boolean handleEvent(Event evt) {
               if(evt.id == Event.ACTION_EVENT) {
                  this.handleCho(evt) ;
                  return true ;
               }
               if(evt.id == Event.SCROLL_ABSOLUTE) {
                  this.handleBar(evt) ;
                  return true ;
               }
               if(evt.id == Event.SCROLL_LINE_DOWN) {
                  this.handleBar(evt) ;
                  return true ;
               }
               if(evt.id == Event.SCROLL_LINE_UP) {
                  this.handleBar(evt) ;
                  return true ;
               }
               if(evt.id == Event.SCROLL_PAGE_DOWN) {
                  this.handleBar(evt) ;
                  return true ;
               }
               if(evt.id == Event.SCROLL_PAGE_UP) {
                  this.handleBar(evt) ;
                  return true ;
               }
               else return false ;
          }

          public void handleBar(Event evt) {
             int i1,i2 ;
             double v1,v2 ;
             float fl1,fl2 ;
     // Input for computations
             i1 = s1.getValue() ;
             i2 = s2.getValue() ;

             vfsd   = v1 = i1 * (vmx - vmn)/ 1000. + vmn ;
             alt    = v2 = i2 * (almx - almn)/ 1000. + almn ;

             fl1 = (float) v1 ;
             fl2 = (float) v2 ;

             inl.f1.setText(String.valueOf(fl1)) ;
             inl.f2.setText(String.valueOf(fl2)) ;

       //  set limits on spin
             if(foil >= 4) cyl.setLims() ;

             computeFlow() ;
          } // handle bar

          public void handleCho(Event evt) {
             int i1,i2 ;
             double v1,v2 ;
             float fl1,fl2 ;

             planet  = plntch.getSelectedIndex() ;

             if (planet == 2) {
                vfsd = 5. ;
                vmax = 50. ;
                if (lunits == 1) vmax = 80. ;
                alt = 0.0 ;
                altmax = 5000. ;
                area = 10.0 ;
                armax = 50. ;
             }
             else {
                vmax = 250. ;
                if (lunits == 1) vmax = 400. ;
                altmax = 50000. ;
                armax = 2500. ;
             }

             if (planet == 3) {
                inl.o1.setBackground(Color.white) ;
                inl.o1.setForeground(Color.black) ;
                inr.inr2.o2.setBackground(Color.white) ;
                inr.inr2.o2.setForeground(Color.black) ;
             }
             else {
                inl.o1.setBackground(Color.black) ;
                inl.o1.setForeground(Color.yellow) ;
                inr.inr2.o2.setBackground(Color.black) ;
                inr.inr2.o2.setForeground(Color.yellow) ;
             }

             if (planet == 4) {
                inl.o3.setBackground(Color.white) ;
                inl.o3.setForeground(Color.black) ;
                inr.inr3.o4.setBackground(Color.white) ;
                inr.inr3.o4.setForeground(Color.black) ;
             }
             else {
                inl.o3.setBackground(Color.black) ;
                inl.o3.setForeground(Color.yellow) ;
                inr.inr3.o4.setBackground(Color.black) ;
                inr.inr3.o4.setForeground(Color.yellow) ;
             }

             if (planet <= 1) {
                if (foil <= 3) {
                   layplt.show(in.grf.l, "first") ;
                }
                if (foil >= 4) {
                   layplt.show(in.grf.l, "second") ;
                }
             }
             if (planet >= 2) {
                layplt.show(in.grf.l, "second") ;
             }

             layout.show(out, "first")  ;
              con.obt1.setBackground(Color.yellow) ;
              con.obt2.setBackground(Color.white) ;
              con.obt3.setBackground(Color.white) ;
              con.obt4.setBackground(Color.white) ;
              con.obt5.setBackground(Color.white) ;
             outopt = 0 ;
             dispp = 0 ;
             calcrange = 0 ;

             loadInput() ;
          } // handle  choice
        }  // Inr 
     }  // Flt 

     class Shp extends Panel {
        Foil outerparent ;
        Inl inl ;
        Inr inr ;

        Shp (Foil target) {

           outerparent = target ;
           setLayout(new GridLayout(1,2,5,5)) ;

           inl = new Inl(outerparent) ;
           inr = new Inr(outerparent) ;

           add(inl) ;
           add(inr) ;
        }

        class Inl extends Panel {
           Foil outerparent ;
           TextField f1,f2,f3 ;
           Label l1,l2,l3 ;
           Label l01,l02,l03 ;
           Button inl1,inl2,inl3 ;
      
           Inl (Foil target) {
      
            outerparent = target ;
            setLayout(new GridLayout(6,2,2,10)) ;

            l01 = new Label("Airfoil", Label.RIGHT) ;
            l01.setForeground(Color.blue) ;
            l02 = new Label("Shape", Label.LEFT) ;
            l02.setForeground(Color.blue) ;

            l1 = new Label("Camber-%c", Label.CENTER) ;
            f1 = new TextField("0.0",5) ;

            l2 = new Label("Thick-%crd", Label.CENTER) ;
            f2 = new TextField("12.5",5) ;

            l3 = new Label("Angle-deg", Label.CENTER) ;
            f3 = new TextField("5.0",5) ;

            l03 = new Label("Basic Shapes:", Label.RIGHT) ;
            l03.setForeground(Color.black) ;

            inl1 = new Button("Symmetric") ;
            inl1.setBackground(Color.yellow) ;
            inl1.setForeground(Color.blue) ;
            inl2 = new Button("Flat Bottom") ;
            inl2.setBackground(Color.white) ;
            inl2.setForeground(Color.blue) ;
            inl3 = new Button("Neg. Camber") ;
            inl3.setBackground(Color.white) ;
            inl3.setForeground(Color.blue) ;

            add(l01) ;
            add(l02) ;

            add(l3) ;
            add(f3) ;

            add(l1) ;
            add(f1) ;

            add(l2) ;
            add(f2) ;
 
            add(l03) ;
            add(inl1) ;
 
            add(inl2) ;
            add(inl3) ;
          }

          public boolean action(Event evt, Object arg) {
            Double V1,V2,V3 ;
            double v1,v2,v3 ;
            float fl1 ;
            int i1,i2,i3 ;

            if(evt.target instanceof Button) {
              handleBut (evt,arg) ;
              return true ;
            }

            else {
              V1 = Double.valueOf(f1.getText()) ;
              v1 = V1.doubleValue() ;
              V2 = Double.valueOf(f2.getText()) ;
              v2 = V2.doubleValue() ;
              V3 = Double.valueOf(f3.getText()) ;
              v3 = V3.doubleValue() ;

              caminpt = v1 ;
              if(v1 < camn) {
                caminpt = v1 = camn ;
                fl1 = (float) v1 ;
                f1.setText(String.valueOf(fl1)) ;
              }
              if(v1 > camx) {
                caminpt = v1 = camx ;
                fl1 = (float) v1 ;
                f1.setText(String.valueOf(fl1)) ;
              }
              camval = caminpt / 25.0 ;

              thkinpt = v2 ;
              if(v2 < thkmn) {
                thkinpt = v2 = thkmn ;
                fl1 = (float) v2 ;
                f2.setText(String.valueOf(fl1)) ;
              }
              if(v2 > thkmx) {
                thkinpt = v2 = thkmx ;
                fl1 = (float) v2 ;
                f2.setText(String.valueOf(fl1)) ;
              }
              thkval  = thkinpt / 25.0 ;
    
              alfval = v3 ;
              if(v3 < angmn) {
                alfval = v3 = angmn  ;
                fl1 = (float) v3 ;
                f3.setText(String.valueOf(fl1)) ;
              }
              if(v3 > angmx) {
                alfval = v3 = angmx ;
                fl1 = (float) v3 ;
                f3.setText(String.valueOf(fl1)) ;
              }

              i1 = (int) (((v1 - camn)/(camx-camn))*1000.) ;
              i2 = (int) (((v2 - thkmn)/(thkmx-thkmn))*1000.) ;
              i3 = (int) (((v3 - angmn)/(angmx-angmn))*1000.) ;
    
              inr.s1.setValue(i1) ;
              inr.s2.setValue(i2) ;
              inr.s3.setValue(i3) ;

              computeFlow() ;
              return true ;
            }
          } // Handler

          public void handleBut(Event evt, Object arg) {
            float fl1,fl2,fl3 ;
            int i1,i2,i3 ;
            int index ;
            double mapfac; 

            String label = (String)arg ;
   
            foil = 1 ;

            if(label.equals("Symmetric")) {
               inl.inl1.setBackground(Color.yellow) ;
               inl.inl2.setBackground(Color.white) ;
               inl.inl3.setBackground(Color.white) ;
               inr.inr1.inb1.setBackground(Color.white) ;
               inr.inr1.inb2.setBackground(Color.white) ;
               inr.inr2.inb3.setBackground(Color.white) ;
               inr.inr2.inb4.setBackground(Color.white) ;
               alfval = 0.0 ;
               caminpt = 0.0 ;
               thkinpt = 12.5 ;
            }

            if(label.equals("Flat Bottom")) {
               inl.inl1.setBackground(Color.white) ;
               inl.inl2.setBackground(Color.yellow) ;
               inl.inl3.setBackground(Color.white) ;
               inr.inr1.inb1.setBackground(Color.white) ;
               inr.inr1.inb2.setBackground(Color.white) ;
               inr.inr2.inb3.setBackground(Color.white) ;
               inr.inr2.inb4.setBackground(Color.white) ;
               alfval = 7.0 ;
               caminpt = 5.0 ;
               thkinpt = 12.5 ;
            }

            if(label.equals("Neg. Camber")) {
               inl.inl1.setBackground(Color.white) ;
               inl.inl2.setBackground(Color.white) ;
               inl.inl3.setBackground(Color.yellow) ;
               inr.inr1.inb1.setBackground(Color.white) ;
               inr.inr1.inb2.setBackground(Color.white) ;
               inr.inr2.inb3.setBackground(Color.white) ;
               inr.inr2.inb4.setBackground(Color.white) ;
               alfval = -7.0 ;
               caminpt = -5.0 ;
               thkinpt = 12.5 ;
            }

            camval = caminpt / 25.0 ;
            thkval = thkinpt / 25.0 ;

            inl.f1.setText(String.valueOf(caminpt)) ;
            inl.f2.setText(String.valueOf(thkinpt)) ;
            inl.f3.setText(String.valueOf(alfval)) ;

            i1 = (int) (((caminpt - camn)/(camx-camn))*1000.) ;
            i2 = (int) (((thkinpt - thkmn)/(thkmx-thkmn))*1000.) ;
            i3 = (int) (((alfval - angmn)/(angmx-angmn))*1000.) ;
    
            inr.s1.setValue(i1) ;
            inr.s2.setValue(i2) ;
            inr.s3.setValue(i3) ;

            inr.shapch.select(0);
            in.cyl.inr.shapch.select(0);

            computeFlow() ;
          }
        }  // Inl 

        class Inr extends Panel {
           Foil outerparent ;
           Scrollbar s1,s2,s3;
           Choice shapch ;
           Inr1 inr1 ;
           Inr2 inr2 ;

           Inr (Foil target) {
            int i1,i2,i3 ;

            outerparent = target ;
            setLayout(new GridLayout(6,1,2,10)) ;

            inr1 = new Inr1(outerparent) ;
            inr2 = new Inr2(outerparent) ;

            i1 = (int) (((0.0 - camn)/(camx-camn))*1000.) ;
            i2 = (int) (((12.5 - thkmn)/(thkmx-thkmn))*1000.) ;
            i3 = (int) (((alfval - angmn)/(angmx-angmn))*1000.) ;

            s1 = new Scrollbar(Scrollbar.HORIZONTAL,i1,10,0,1000);
            s2 = new Scrollbar(Scrollbar.HORIZONTAL,i2,10,0,1000);
            s3 = new Scrollbar(Scrollbar.HORIZONTAL,i3,10,0,1000);

            shapch = new Choice() ;
            shapch.addItem("Airfoil") ;
            shapch.addItem("Ellipse");
            shapch.addItem("Plate");
            shapch.addItem("Cylinder");
            shapch.addItem("Ball");
            shapch.setBackground(Color.white) ;
            shapch.setForeground(Color.blue) ;
            shapch.select(0) ;

            add(shapch) ;
            add(s3) ;
            add(s1) ;
            add(s2) ;
            add(inr1) ;
            add(inr2) ;
          }

          public boolean handleEvent(Event evt) {
               if(evt.id == Event.ACTION_EVENT) {
                  this.handleCho(evt) ;
                  return true ;
               }
               if(evt.id == Event.SCROLL_ABSOLUTE) {
                  this.handleBar(evt) ;
                  return true ;
               }
               if(evt.id == Event.SCROLL_LINE_DOWN) {
                  this.handleBar(evt) ;
                  return true ;
               }
               if(evt.id == Event.SCROLL_LINE_UP) {
                  this.handleBar(evt) ;
                  return true ;
               }
               if(evt.id == Event.SCROLL_PAGE_DOWN) {
                  this.handleBar(evt) ;
                  return true ;
               }
               if(evt.id == Event.SCROLL_PAGE_UP) {
                  this.handleBar(evt) ;
                  return true ;
               }
               else return false ;
          }

          public void handleBar(Event evt) {
             int i1,i2,i3 ;
             double v1,v2,v3 ;
             float fl1,fl2,fl3 ;
              
    // Input for computations
             i1 = s1.getValue() ;
             i2 = s2.getValue() ;
             i3 = s3.getValue() ;

             caminpt = v1 = i1 * (camx - camn)/ 1000. + camn ;
             camval  = caminpt / 25.0 ;
             thkinpt = v2 = i2 * (thkmx - thkmn)/ 1000. + thkmn ;
             thkval  = thkinpt / 25.0 ;
             alfval  = v3 = i3 * (angmx - angmn)/ 1000. + angmn ;

             fl1 = (float) v1 ;
             fl2 = (float) v2 ;
             fl3 = (float) v3 ;

             inl.f1.setText(String.valueOf(fl1)) ;
             inl.f2.setText(String.valueOf(fl2)) ;
             inl.f3.setText(String.valueOf(fl3)) ;
     
             computeFlow() ;
           }

           public void handleCho(Event evt) {
             int i2 ;
             double v2 ;
             float fl1 ;

             foil  = shapch.getSelectedIndex() + 1 ;
             if (foil >= 4) alfval = 0.0 ;
             if(foil <= 2) layin.show(in, "second")  ;
             if(foil == 3) {
                layin.show(in, "second")  ;
                thkinpt = v2 = thkmn ;
                thkval  = thkinpt / 25.0 ;
                fl1 = (float) v2 ;
                inl.f2.setText(String.valueOf(fl1)) ;
                i2 = (int) (((v2 - thkmn)/(thkmx-thkmn))*1000.) ;
                inr.s2.setValue(i2) ;
             }
             if(foil == 4) {
                 layin.show(in, "fifth")  ;
                 in.anl.cbt1.setBackground(Color.white) ;
                 in.anl.cbt2.setBackground(Color.white) ;
                 in.anl.cbt3.setBackground(Color.white) ;
             }
             if(foil == 5) {
                 span = radius ;
                 area = 3.1415926*radius*radius ;
                 layin.show(in, "fifth")  ;
                 if (viewflg != 0) viewflg = 0 ;
                 bdragflag = 1 ;
                 in.anl.cbt1.setBackground(Color.yellow) ;
                 in.anl.cbt2.setBackground(Color.white) ;
                 in.anl.cbt3.setBackground(Color.white) ;
             }
    
             if (foil <= 3) {
                if (planet <= 1) {
                   layplt.show(in.grf.l, "first") ;
                }
                if (planet >= 2) {
                   layplt.show(in.grf.l, "second") ;
                }
                indrag = 1;
                in.anl.bt7.setBackground(Color.yellow) ;
                in.anl.bt8.setBackground(Color.white) ;
                in.anl.cbt1.setBackground(Color.white) ;
                in.anl.cbt2.setBackground(Color.white) ;
                in.anl.cbt3.setBackground(Color.white) ;
             }
             if (foil >= 4) {
                layplt.show(in.grf.l, "second") ;
                indrag = 0;
                in.anl.bt7.setBackground(Color.white) ;
                in.anl.bt8.setBackground(Color.yellow) ;
             }

             in.cyl.inr.shapch.select(foil-1);
             layout.show(out, "first")  ;
              con.obt1.setBackground(Color.yellow) ;
              con.obt2.setBackground(Color.white) ;
              con.obt3.setBackground(Color.white) ;
              con.obt4.setBackground(Color.white) ;
              con.obt5.setBackground(Color.white) ;
             outopt = 0 ;
             dispp = 0 ;
             calcrange = 0 ;

             loadInput() ;
           }

           class Inr1 extends Panel {
              Foil outerparent ;
              Button inb1,inb2 ;

              Inr1 (Foil target) {

                 outerparent = target ;
                 setLayout(new GridLayout(1,2,2,10)) ;

                 inb1 = new Button("High Camber") ;
                 inb1.setBackground(Color.white) ;
                 inb1.setForeground(Color.blue) ;

                 inb2 = new Button("Flat Plate") ;
                 inb2.setBackground(Color.white) ;
                 inb2.setForeground(Color.blue) ;

                 add(inb1) ;
                 add(inb2);
              }

              public boolean action(Event evt, Object arg) {

                if(evt.target instanceof Button) {
                  handleBut (evt,arg) ;
                  return true ;
                }

                else {
                   return false ;
                }
              }

              public void handleBut(Event evt, Object arg) {
                float fl1,fl2,fl3 ;
                int i1,i2,i3 ;

                String label = (String)arg ;
   
                if(label.equals("High Camber")) {
                   inl.inl1.setBackground(Color.white) ;
                   inl.inl2.setBackground(Color.white) ;
                   inl.inl3.setBackground(Color.white) ;
                   inb1.setBackground(Color.yellow) ;
                   inb2.setBackground(Color.white) ;
                   inr.inr2.inb3.setBackground(Color.white) ;
                   inr.inr2.inb4.setBackground(Color.white) ;
                   foil = 1 ;
                   alfval = 9.0 ;
                   caminpt = 15.0 ;
                   thkinpt = 12.5 ;
                   shapch.select(0);
                   in.cyl.inr.shapch.select(0);
                }

                if(label.equals("Flat Plate")) {
                   inl.inl1.setBackground(Color.white) ;
                   inl.inl2.setBackground(Color.white) ;
                   inl.inl3.setBackground(Color.white) ;
                   inb1.setBackground(Color.white) ;
                   inb2.setBackground(Color.yellow) ;
                   inr.inr2.inb3.setBackground(Color.white) ;
                   inr.inr2.inb4.setBackground(Color.white) ;
                   foil = 3 ;
                   alfval = 5.0 ;
                   caminpt = 0.0 ;
                   thkinpt = 1.0 ;
                   shapch.select(2);
                   in.cyl.inr.shapch.select(2);
                }

                camval = caminpt / 25.0 ;
                thkval = thkinpt / 25.0 ;

                inl.f1.setText(String.valueOf(caminpt)) ;
                inl.f2.setText(String.valueOf(thkinpt)) ;
                inl.f3.setText(String.valueOf(alfval)) ;

                i1 = (int) (((caminpt - camn)/(camx-camn))*1000.) ;
                i2 = (int) (((thkinpt - thkmn)/(thkmx-thkmn))*1000.) ;
                i3 = (int) (((alfval - angmn)/(angmx-angmn))*1000.) ;
    
                inr.s1.setValue(i1) ;
                inr.s2.setValue(i2) ;
                inr.s3.setValue(i3) ;

                computeFlow() ;
              }
            }  // Inr1

           class Inr2 extends Panel {
              Foil outerparent ;
              Button inb3,inb4 ;

              Inr2 (Foil target) {

                 outerparent = target ;
                 setLayout(new GridLayout(1,2,2,10)) ;

                 inb3 = new Button("Ellipse") ;
                 inb3.setBackground(Color.white) ;
                 inb3.setForeground(Color.blue) ;

                 inb4 = new Button("Curve Plate") ;
                 inb4.setBackground(Color.white) ;
                 inb4.setForeground(Color.blue) ;

                 add(inb3) ;
                 add(inb4);
              }

              public boolean action(Event evt, Object arg) {

                if(evt.target instanceof Button) {
                  handleBut (evt,arg) ;
                  return true ;
                }

                else {
                   return false ;
                }
              }

              public void handleBut(Event evt, Object arg) {
                float fl1,fl2,fl3 ;
                int i1,i2,i3 ;

                String label = (String)arg ;
   
                if(label.equals("Ellipse")) {
                   inl.inl1.setBackground(Color.white) ;
                   inl.inl2.setBackground(Color.white) ;
                   inl.inl3.setBackground(Color.white) ;
                   inr.inr1.inb1.setBackground(Color.white) ;
                   inr.inr1.inb2.setBackground(Color.white) ;
                   inb3.setBackground(Color.yellow) ;
                   inb4.setBackground(Color.white) ;
                   foil = 2 ;
                   alfval = 0.0 ;
                   caminpt = 0.0 ;
                   thkinpt = 12.5 ;
                   shapch.select(1);
                   in.cyl.inr.shapch.select(1);
                }

                if(label.equals("Curve Plate")) {
                   inl.inl1.setBackground(Color.white) ;
                   inl.inl2.setBackground(Color.white) ;
                   inl.inl3.setBackground(Color.white) ;
                   inr.inr1.inb1.setBackground(Color.white) ;
                   inr.inr1.inb2.setBackground(Color.white) ;
                   inb3.setBackground(Color.white) ;
                   inb4.setBackground(Color.yellow) ;
                   foil = 3 ;
                   alfval = 5.0 ;
                   caminpt = 5.0 ;
                   thkinpt = 1.0 ;
                   shapch.select(2);
                   in.cyl.inr.shapch.select(2);
                }

                camval = caminpt / 25.0 ;
                thkval = thkinpt / 25.0 ;

                inl.f1.setText(String.valueOf(caminpt)) ;
                inl.f2.setText(String.valueOf(thkinpt)) ;
                inl.f3.setText(String.valueOf(alfval)) ;

                i1 = (int) (((caminpt - camn)/(camx-camn))*1000.) ;
                i2 = (int) (((thkinpt - thkmn)/(thkmx-thkmn))*1000.) ;
                i3 = (int) (((alfval - angmn)/(angmx-angmn))*1000.) ;
    
                inr.s1.setValue(i1) ;
                inr.s2.setValue(i2) ;
                inr.s3.setValue(i3) ;

                computeFlow() ;
              }
            }  // Inr2
        }  // Inr
     }  // Shp 

     class Siz extends Panel {
        Foil outerparent ;
        Inl inl ;
        Inr inr ;

        Siz (Foil target) {

           outerparent = target ;
           setLayout(new GridLayout(1,2,5,5)) ;

           inl = new Inl(outerparent) ;
           inr = new Inr(outerparent) ;

           add(inl) ;
           add(inr) ;
        }

        class Inl extends Panel {
           Foil outerparent ;
           TextField f1,f2,f3,o4 ;
           Label l1,l2,l3,l4 ;
           Label l01,l02 ;
    
           Inl (Foil target) {
   
            outerparent = target ;
            setLayout(new GridLayout(5,2,2,10)) ;

            l01 = new Label("Wing", Label.RIGHT) ;
            l01.setForeground(Color.blue) ;
            l02 = new Label("Size", Label.LEFT) ;
            l02.setForeground(Color.blue) ;

            l1 = new Label("Chord-ft", Label.CENTER) ;
            f1 = new TextField("5.0",5) ;

            l2 = new Label("Span-ft", Label.CENTER) ;
            f2 = new TextField("20.0",5) ;

            l3 = new Label("Area-sq ft", Label.CENTER) ;
            f3 = new TextField("100.0",5) ;

            l4 = new Label("Aspect Rat", Label.CENTER) ;
            o4 = new TextField("0.0",5) ;
            o4.setBackground(Color.black) ;
            o4.setForeground(Color.yellow) ;

            add(l01) ;
            add(l02) ;

            add(l1) ;
            add(f1) ;

            add(l2) ;
            add(f2) ;

            add(l3) ;
            add(f3) ;

            add(l4) ;
            add(o4) ;
         }

          public boolean handleEvent(Event evt) {
            Double V1,V2,V3 ;
            double v1,v2,v3 ;
            float fl1 ;
            int i1,i2,i3,choice ;

            if(evt.id == Event.ACTION_EVENT) {
              V1 = Double.valueOf(f1.getText()) ;
              v1 = V1.doubleValue() ;
              V2 = Double.valueOf(f2.getText()) ;
              v2 = V2.doubleValue() ;
              V3 = Double.valueOf(f3.getText()) ;
              v3 = V3.doubleValue() ;

              chord = v1 ;
              if(v1 < chrdmn) {
                chord = v1 = chrdmn ;
                fl1 = (float) v1 ;
                f1.setText(String.valueOf(fl1)) ;
              }
              if(v1 > chrdmx) {
                chord = v1 = chrdmx ;
                fl1 = (float) v1 ;
                f1.setText(String.valueOf(fl1)) ;
              }

              span = v2 ;
              if(v2 < spanmn) {
                span = v2 = spanmn ;
                fl1 = (float) v2 ;
                f2.setText(String.valueOf(fl1)) ;
              }
              if(v2 > spanmx) {
                span = v2 = spanmx ;
                fl1 = (float) v2 ;
                f2.setText(String.valueOf(fl1)) ;
              }
   
              area = v3 ;
              if(v3 < armn) {
                area = v3 = armn  ;
                fl1 = (float) v3 ;
                f3.setText(String.valueOf(fl1)) ;
              }
              if(v3 > armx) {
                area = v3 = armx ;
                fl1 = (float) v3 ;
                f3.setText(String.valueOf(fl1)) ;
              }

        // keeping consistent
             choice = 0 ;
             if (chord >= (chrdold+.01) || chord <= (chrdold-.01))choice = 1;
             if (span >= (spnold+.1) || span <= (spnold-.1)) choice = 2;
             if (area >= (arold+1.0) || area <= (arold-1.0)) choice = 3;
             switch(choice) {
                case 1: {          // chord changed
                  if (chord < span) {
                    v3 = span * chord ;
                    aspr = span*span/v3 ;
                  }
                  if (chord >= span) {
                    v2 = chord ;
                    aspr = 1.0 ;
                    v3 = v2 * chord ;
                    fl1 = (float) v2 ;
                    f2.setText(String.valueOf(fl1)) ;
                    spnold = span = v2 ;
                  }
                  fl1 = (float) v3 ;
                  f3.setText(String.valueOf(fl1)) ;
                  if (viewflg == 2) {
                    fact = fact * chord/chrdold ;
                  }
                  chrdold = chord ;
                  arold = area = v3 ;
                  break ;
                }
                case 2: {          // span changed
                  if (span > chord) {
                    v3 = span * chord ;
                    aspr = span*span/v3 ;
                  }
                  if (span <= chord) {
                     v1 = span ;
                     aspr = 1.0 ;
                     v3 = v1 * span ;
                     fl1 = (float) v1 ;
                     f1.setText(String.valueOf(fl1)) ;
                     chord = v1 ;
                     if (viewflg == 2) {
                       fact = fact * chord/chrdold ;
                     }
                     chrdold = chord ;
                   }
                   fl1 = (float) v3 ;
                   f3.setText(String.valueOf(fl1)) ;
                   spnold = span ;
                   arold = area = v3 ;
                   break ;
                }
                case 3: {          // area changed
                   v2 = Math.sqrt(area*aspr) ;
                   v1 = area / v2 ;
                   fl1 = (float) v1 ;
                   f1.setText(String.valueOf(fl1)) ;
                   fl1 = (float) v2 ;
                   f2.setText(String.valueOf(fl1)) ;
                   chord = v1 ;
                   if (viewflg == 2) {
                     fact = fact * chord/chrdold ;
                   }
                   chrdold = chord ;
                   spnold = span = v2 ;
                   arold = area ;
                }
              }
              spanfac = (int)(2.0*fact*aspr*.3535) ;

              i1 = (int) (((v1 - chrdmn)/(chrdmx-chrdmn))*1000.) ;
              i2 = (int) (((v2 - spanmn)/(spanmx-spanmn))*1000.) ;
              i3 = (int) (((v3 - armn)/(armx-armn))*1000.) ;
   
              inr.sld1.s1.setValue(i1) ;
              inr.sld2.s2.setValue(i2) ;
              inr.sld3.s3.setValue(i3) ;

              computeFlow() ;
              return true ;
            }
            else return false ;
          } // Handler
        }  // Inl 

        class Inr extends Panel {
           Foil outerparent ;
           Sld1 sld1 ;
           Sld2 sld2 ;
           Sld3 sld3 ;

           Inr (Foil target) {
            int i1,i2,i3 ;

            outerparent = target ;
            setLayout(new GridLayout(5,1,2,10)) ;

            i1 = (int) (((chord - chrdmn)/(chrdmx-chrdmn))*1000.) ;
            i2 = (int) (((span - spanmn)/(spanmx-spanmn))*1000.) ;
            i3 = (int) (((area - armn)/(armx-armn))*1000.) ;

            sld1 = new Sld1(outerparent) ;
            sld2 = new Sld2(outerparent) ;
            sld3 = new Sld3(outerparent) ;

            add(new Label(" ", Label.CENTER)) ;
            add(sld1) ;
            add(sld2) ;
            add(sld3) ;
            add(new Label(" ", Label.CENTER)) ;
          }

          class Sld1 extends Panel {  // chord slider
             Foil outerparent ;
             Scrollbar s1 ; 

             Sld1 (Foil target) {
              int i1 ;

               outerparent = target ;
               setLayout(new GridLayout(1,1,0,0)) ;

               i1 = (int) (((chord - chrdmn)/(chrdmx-chrdmn))*1000.) ;

               s1 = new Scrollbar(Scrollbar.HORIZONTAL,i1,10,0,1000);

               add(s1) ;
             }

             public boolean handleEvent(Event evt) {
                  if(evt.id == Event.ACTION_EVENT) {
                     this.handleBar(evt) ;
                     return true ;
                  }
                  if(evt.id == Event.SCROLL_ABSOLUTE) {
                     this.handleBar(evt) ;
                  return true ;
                  }
                  if(evt.id == Event.SCROLL_LINE_DOWN) {
                     this.handleBar(evt) ;
                     return true ;
                  }
                  if(evt.id == Event.SCROLL_LINE_UP) {
                     this.handleBar(evt) ;
                     return true ;
                  }
                  if(evt.id == Event.SCROLL_PAGE_DOWN) {
                     this.handleBar(evt) ;
                     return true ;
                  }
                  if(evt.id == Event.SCROLL_PAGE_UP) {
                     this.handleBar(evt) ;
                     return true ;
                  }
                  else return false ;
             }

             public void handleBar(Event evt) {
                int i1,i3 ;
                double v1 ;
                float fl1,fl3 ;

       // Input for computations
                i1 = s1.getValue() ;

                chord  = v1 = i1 * (chrdmx - chrdmn)/ 1000. + chrdmn ;

                if (chord >= span) {  // limit apsect ratio to 1.0
                      chord = v1 = span ;
                      i1 = (int) (((chord - chrdmn)/(chrdmx-chrdmn))*1000.) ;
                      s1.setValue(i1) ;
                }

                area = span * chord ;
                aspr = span*span/area ;
                i3 = (int) (((area - armn)/(armx-armn))*1000.) ;
                sld3.s3.setValue(i3) ;

                if (viewflg == 2) {
                   fact = fact * chord/chrdold ;
                }

                spanfac = (int)(2.0*fact*aspr*.3535) ;

                arold = area ;
                chrdold = chord ;

                fl1 = (float) v1 ;
                fl3 = (float) area ;

                inl.f1.setText(String.valueOf(fl1)) ;
                inl.f3.setText(String.valueOf(fl3)) ;
       
                computeFlow() ;
              }  // handler for scroll
          }  // sld1

          class Sld2 extends Panel {  // span slider
             Foil outerparent ;
             Scrollbar s2 ; 

             Sld2 (Foil target) {
              int i2 ;

               outerparent = target ;
               setLayout(new GridLayout(1,1,0,0)) ;

               i2 = (int) (((span - spanmn)/(spanmx-spanmn))*1000.) ;

               s2 = new Scrollbar(Scrollbar.HORIZONTAL,i2,10,0,1000);

               add(s2) ;
             }

             public boolean handleEvent(Event evt) {
                  if(evt.id == Event.ACTION_EVENT) {
                     this.handleBar(evt) ;
                     return true ;
                  }
                  if(evt.id == Event.SCROLL_ABSOLUTE) {
                     this.handleBar(evt) ;
                  return true ;
                  }
                  if(evt.id == Event.SCROLL_LINE_DOWN) {
                     this.handleBar(evt) ;
                     return true ;
                  }
                  if(evt.id == Event.SCROLL_LINE_UP) {
                     this.handleBar(evt) ;
                     return true ;
                  }
                  if(evt.id == Event.SCROLL_PAGE_DOWN) {
                     this.handleBar(evt) ;
                     return true ;
                  }
                  if(evt.id == Event.SCROLL_PAGE_UP) {
                     this.handleBar(evt) ;
                     return true ;
                  }
                  else return false ;
             }

             public void handleBar(Event evt) {
                int i2,i3 ;
                double v2 ;
                float fl2,fl3 ;
  
      // Input for computations
                i2 = s2.getValue() ;

                span   = v2 = i2 * (spanmx - spanmn)/ 1000. + spanmn ;

                if (span <= chord) {  // limit apsect ratio to 1.0
                      span = v2 = chord ;
                      i2 = (int) (((span - spanmn)/(spanmx-spanmn))*1000.) ;
                      s2.setValue(i2) ;
                }

                area = span * chord ;
                aspr = span*span/area ;
                i3 = (int) (((area - armn)/(armx-armn))*1000.) ;
                sld3.s3.setValue(i3) ;

                arold = area ;
                spnold = span ;

                spanfac = (int)(2.0*fact*aspr*.3535) ;

                fl2 = (float) v2 ;
                fl3 = (float) area ;
  
                inl.f2.setText(String.valueOf(fl2)) ;
                inl.f3.setText(String.valueOf(fl3)) ;
          
                computeFlow() ;
              }  // handler for scroll
           }  // sld2

          class Sld3 extends Panel {  // area slider
             Foil outerparent ;
             Scrollbar s3 ; 
 
             Sld3 (Foil target) {
              int i3 ;
 
               outerparent = target ;
               setLayout(new GridLayout(1,1,0,0)) ;
  
               i3 = (int) (((area - armn)/(armx-armn))*1000.) ;
 
               s3 = new Scrollbar(Scrollbar.HORIZONTAL,i3,10,0,1000);

               add(s3) ;
             }

             public boolean handleEvent(Event evt) {
                  if(evt.id == Event.ACTION_EVENT) {
                     this.handleBar(evt) ;
                     return true ;
                  }
                  if(evt.id == Event.SCROLL_ABSOLUTE) {
                     this.handleBar(evt) ;
                  return true ;
                  }
                  if(evt.id == Event.SCROLL_LINE_DOWN) {
                     this.handleBar(evt) ;
                     return true ;
                  }
                  if(evt.id == Event.SCROLL_LINE_UP) {
                     this.handleBar(evt) ;
                     return true ;
                  }
                  if(evt.id == Event.SCROLL_PAGE_DOWN) {
                     this.handleBar(evt) ;
                     return true ;
                  }
                  if(evt.id == Event.SCROLL_PAGE_UP) {
                     this.handleBar(evt) ;
                     return true ;
                  }
                  else return false ;
             }
 
             public void handleBar(Event evt) {
                int i1,i2,i3 ;
                double v1,v2,v3 ;
                float fl1,fl2,fl3 ;
 
       // Input for computations
                i3 = s3.getValue() ;

                area   = v3 = i3 * (armx - armn)/ 1000. + armn ;
 
                v2 = span = Math.sqrt(area*aspr) ;
                v1 = chord = area / v2 ;
                i1 = (int) (((v1 - chrdmn)/(chrdmx-chrdmn))*1000.) ;
                i2 = (int) (((v2 - spanmn)/(spanmx-spanmn))*1000.) ;
                sld1.s1.setValue(i1) ;
                sld2.s2.setValue(i2) ;

                if (viewflg == 2) {
                   fact = fact * chord/chrdold ;
                }
                spanfac = (int)(2.0*fact*aspr*.3535) ;

                arold = area ;
                spnold = span ;
                chrdold = chord ;

                fl1 = (float) v1 ;
                fl2 = (float) v2 ;
                fl3 = (float) v3 ;

                inl.f1.setText(String.valueOf(fl1)) ;
                inl.f2.setText(String.valueOf(fl2)) ;
                inl.f3.setText(String.valueOf(fl3)) ;
        
                computeFlow() ;
              }  // handler for scroll
           }  // sld3
        }     // Inr
     }  // Siz 

     class Cyl extends Panel {
        Foil outerparent ;
        Inl inl ;
        Inr inr ;

        Cyl (Foil target) {

           outerparent = target ;
           setLayout(new GridLayout(1,2,5,5)) ;

           inl = new Inl(outerparent) ;
           inr = new Inr(outerparent) ;

           add(inl) ;
           add(inr) ;
        }

        public void setLims() {
           Double V1 ;
           double v1 ;
           float fl1 ;
           int i1 ;

           spinmx = 2.75 * vfsd/vconv /(radius/lconv) ;
           spinmn = -2.75 * vfsd/vconv/(radius/lconv) ;
           if(spin*60.0 < spinmn) {
               spin = spinmn/60.0 ;
               fl1 = (float) (spin*60.0)  ;
               inl.f1.setText(String.valueOf(fl1)) ;
           }
           if(spin*60.0 > spinmx) {
               spin = spinmx/60.0 ;
               fl1 = (float) (spin*60.0)  ;
               inl.f1.setText(String.valueOf(fl1)) ;
           }
           i1 = (int) (((60*spin - spinmn)/(spinmx-spinmn))*1000.) ;
           inr.s1.setValue(i1) ;
        }

        class Inl extends Panel {
           Foil outerparent ;
           TextField f1,f2,f3 ;
           Label l1,l2,l3 ;
           Label l01,l02 ;
     
           Inl (Foil target) {
     
            outerparent = target ;
            setLayout(new GridLayout(5,2,2,10)) ;

            l01 = new Label("Cylinder-", Label.RIGHT) ;
            l01.setForeground(Color.blue) ;
            l02 = new Label("Ball Input", Label.LEFT) ;
            l02.setForeground(Color.blue) ;

            l1 = new Label("Spin rpm", Label.CENTER) ;
            f1 = new TextField("0.0",5) ;

            l2 = new Label("Radius ft", Label.CENTER) ;
            f2 = new TextField(".5",5) ;

            l3 = new Label("Span ft", Label.CENTER) ;
            f3 = new TextField("5.0",5) ;

            add(l01) ;
            add(l02) ;

            add(l1) ;
            add(f1) ;

            add(l2) ;
            add(f2) ;

            add(l3) ;
            add(f3) ;

            add(new Label(" ", Label.CENTER)) ;
            add(new Label(" ", Label.CENTER)) ;
         }

         public boolean handleEvent(Event evt) {
            Double V1,V2,V3 ;
            double v1,v2,v3 ;
            float fl1 ;
            int i1,i2,i3 ;

            if(evt.id == Event.ACTION_EVENT) {
              V1 = Double.valueOf(f1.getText()) ;
              v1 = V1.doubleValue() ;
              V2 = Double.valueOf(f2.getText()) ;
              v2 = V2.doubleValue() ;
              V3 = Double.valueOf(f3.getText()) ;
              v3 = V3.doubleValue() ;

              spin = v1 ;
              if(v1 < spinmn) {
                spin = v1 = spinmn ;
                fl1 = (float) v1 ;
                f1.setText(String.valueOf(fl1)) ;
              }
              if(v1 > spinmx) {
                spin = v1 = spinmx ;
                fl1 = (float) v1 ;
                f1.setText(String.valueOf(fl1)) ;
              }
              spin = spin/60.0 ;

              radius = v2 ;
              if(v2 < radmn) {
                radius = v2 = radmn ;
                fl1 = (float) v2 ;
                f2.setText(String.valueOf(fl1)) ;
              }
              if(v2 > radmx) {
                radius = v2 = radmx ;
                fl1 = (float) v2 ;
                f2.setText(String.valueOf(fl1)) ;
              }
              cyl.setLims() ;
   
              span = v3 ;
              if (foil == 5) {
                span = v3 = radius ;
                fl1 = (float) v3 ;
                f3.setText(String.valueOf(fl1)) ;
              }
              if(v3 < spanmn) {
                span = v3 = spanmn ;
                fl1 = (float) v3 ;
                f3.setText(String.valueOf(fl1)) ;
              }
              if(v3 > spanmx) {
                span = v3 = spanmx ;
                fl1 = (float) v3 ;
                f3.setText(String.valueOf(fl1)) ;
              }
              spanfac = (int)(fact*span/radius*.3535) ;
              area = 2.0*radius*span ;
              if (foil ==5) area = 3.1415926 * radius * radius ;

              i1 = (int) (((v1 - spinmn)/(spinmx-spinmn))*1000.) ;
              i2 = (int) (((v2 - radmn)/(radmx-radmn))*1000.) ;
              i3 = (int) (((v3 - spanmn)/(spanmx-spanmn))*1000.) ;
   
              inr.s1.setValue(i1) ;
              inr.s2.setValue(i2) ;
              inr.s3.setValue(i3) ;

              computeFlow() ;
              return true ;
            }
            else return false ;
          } // Handler
        }  // Inl 

        class Inr extends Panel {
           Foil outerparent ;
           Scrollbar s1,s2,s3;
           Choice shapch ;

           Inr (Foil target) {
             int i1,i2,i3 ;

            outerparent = target ;
            setLayout(new GridLayout(5,1,2,10)) ;

            i1 = (int) (((spin*60.0 - spinmn)/(spinmx-spinmn))*1000.) ;
            i2 = (int) (((radius - radmn)/(radmx-radmn))*1000.) ;
            i3 = (int) (((span - spanmn)/(spanmx-spanmn))*1000.) ;

            s1 = new Scrollbar(Scrollbar.HORIZONTAL,i1,10,0,1000);
            s2 = new Scrollbar(Scrollbar.HORIZONTAL,i2,10,0,1000);
            s3 = new Scrollbar(Scrollbar.HORIZONTAL,i3,10,0,1000);

            shapch = new Choice() ;
            shapch.addItem("Airfoil") ;
            shapch.addItem("Ellipse");
            shapch.addItem("Plate");
            shapch.addItem("Cylinder");
            shapch.addItem("Ball");
            shapch.setBackground(Color.white) ;
            shapch.setForeground(Color.blue) ;
            shapch.select(0) ;

            add(shapch) ;
            add(s1) ;
            add(s2) ;
            add(s3) ;
            add(new Label(" ", Label.CENTER)) ;
          }

          public boolean handleEvent(Event evt) {
               if(evt.id == Event.ACTION_EVENT) {
                  this.handleCho(evt) ;
                  return true ;
               }
               if(evt.id == Event.SCROLL_ABSOLUTE) {
                  this.handleBar(evt) ;
                  return true ;
               }
               if(evt.id == Event.SCROLL_LINE_DOWN) {
                  this.handleBar(evt) ;
                  return true ;
               }
               if(evt.id == Event.SCROLL_LINE_UP) {
                  this.handleBar(evt) ;
                  return true ;
               }
               if(evt.id == Event.SCROLL_PAGE_DOWN) {
                  this.handleBar(evt) ;
                  return true ;
               }
               if(evt.id == Event.SCROLL_PAGE_UP) {
                  this.handleBar(evt) ;
                  return true ;
               }
               else return false ;
          }

          public void handleBar(Event evt) {
             int i1,i2,i3 ;
             double v1,v2,v3 ;
             float fl1,fl2,fl3 ;
              
    // Input for computations
             i1 = s1.getValue() ;
             i2 = s2.getValue() ;
             i3 = s3.getValue() ;

             spin = v1 = i1 * (spinmx - spinmn)/ 1000. + spinmn ;
             spin = spin / 60.0 ;
             radius = v2 = i2 * (radmx - radmn)/ 1000. + radmn ;
             span = v3 = i3 * (spanmx - spanmn)/ 1000. + spanmn ;
             if (foil == 5) span = v3 = radius ;
             spanfac = (int)(fact*span/radius*.3535) ;
             area = 2.0*radius*span ;
             if (foil ==5) area = 3.1415926 * radius * radius ;
             cyl.setLims() ;

             fl1 = (float) v1 ;
             fl2 = (float) v2 ;
             fl3 = (float) v3 ;

             inl.f1.setText(String.valueOf(fl1)) ;
             inl.f2.setText(String.valueOf(fl2)) ;
             inl.f3.setText(String.valueOf(fl3)) ;
      
             computeFlow() ;
           }

           public void handleCho(Event evt) {
             int i2 ;
             double v2 ;
             float fl1 ;

             foil  = shapch.getSelectedIndex() + 1 ;
             if (foil >= 4) alfval = 0.0 ;
             if(foil <= 2) layin.show(in, "second")  ;
             if(foil == 3) {
                layin.show(in, "second")  ;
                thkinpt = v2 = thkmn ;
                thkval  = thkinpt / 25.0 ;
                fl1 = (float) v2 ;
                in.shp.inl.f2.setText(String.valueOf(fl1)) ;
                i2 = (int) (((v2 - thkmn)/(thkmx-thkmn))*1000.) ;
                in.shp.inr.s2.setValue(i2) ;
             }
             if(foil == 4) {
                 layin.show(in, "fifth")  ;
                 in.anl.cbt1.setBackground(Color.white) ;
                 in.anl.cbt2.setBackground(Color.white) ; 
                 in.anl.cbt3.setBackground(Color.white) ; 
             }
             if(foil == 5) {
                 span = radius ;
                 area = 3.1415926*radius*radius ;
                 layin.show(in, "fifth")  ;
                 if (viewflg != 0) viewflg = 0 ;
                 bdragflag = 1;
                 in.anl.cbt1.setBackground(Color.yellow) ;
                 in.anl.cbt2.setBackground(Color.white) ; 
                 in.anl.cbt3.setBackground(Color.white) ; 
             }
     
             if (foil <= 3) {
                if (planet <= 1) {
                   layplt.show(in.grf.l, "first") ;
                }
                if (planet >= 2) {
                   layplt.show(in.grf.l, "second") ;
                }
                indrag = 1 ;
                in.anl.bt7.setBackground(Color.yellow) ;
                in.anl.bt8.setBackground(Color.white) ;
                in.anl.cbt1.setBackground(Color.white) ;
                in.anl.cbt2.setBackground(Color.white) ; 
                in.anl.cbt3.setBackground(Color.white) ; 
             }
             if (foil >= 4) {
                layplt.show(in.grf.l, "second") ;
                indrag = 0 ;
                in.anl.bt7.setBackground(Color.white) ;
                in.anl.bt8.setBackground(Color.yellow) ;
             }

             in.shp.inr.shapch.select(foil-1);
             layout.show(out, "first")  ;
              con.obt1.setBackground(Color.yellow) ;
              con.obt2.setBackground(Color.white) ;
              con.obt3.setBackground(Color.white) ;
              con.obt4.setBackground(Color.white) ;
              con.obt5.setBackground(Color.white) ;
             outopt = 0 ;
             dispp = 0 ;
             calcrange = 0 ;

             loadInput() ;
           } // handler
        }  // Inr
     }  // Cyl 

     class Grf extends Panel {
        Foil outerparent ;
        U u;
        L l;

        Grf (Foil target) {
           outerparent = target ;
           setLayout(new GridLayout(2,1,5,5)) ;

           u = new U(outerparent) ;
           l = new L(outerparent) ;

           add (u) ;
           add (l) ;
        }

        class U extends Panel {
           Foil outerparent ;
           Label l1,l4 ;
           Button pl1,pl2,pl3,pl4;

           U (Foil target) {
              outerparent = target ;
              setLayout(new GridLayout(3,4,5,5)) ;
  
              l1 = new Label("Surface", Label.RIGHT) ;
              l1.setForeground(Color.blue) ;
 
              pl1 = new Button("Pressure") ;
              pl1.setBackground(Color.yellow) ;
              pl1.setForeground(Color.blue) ;

              pl2 = new Button("Velocity") ;
              pl2.setBackground(Color.white) ;
              pl2.setForeground(Color.blue) ;

              pl3 = new Button("Drag Polar") ;
              pl3.setBackground(Color.white) ;
              pl3.setForeground(Color.blue) ;

              l4 = new Label("Generating", Label.RIGHT) ;
              l4.setForeground(Color.blue) ;
              pl4 = new Button("Plane") ;
              pl4.setBackground(Color.white) ;
              pl4.setForeground(Color.blue) ;

              add(new Label("  ", Label.RIGHT)) ;
              add(new Label("Select", Label.RIGHT)) ;
              add(new Label("Plot ", Label.LEFT)) ;
              add(new Label(" ", Label.RIGHT)) ;
 
              add(l1) ;
              add(pl1) ;
              add(pl2) ;
              add(new Label(" ", Label.RIGHT)) ;
 
              add(pl3) ;
              add(new Label(" ", Label.RIGHT)) ;
              add(l4) ;
              add(pl4) ;

           }

           public boolean action(Event evt, Object arg) {
             if(evt.target instanceof Button) {
                handleBut(evt,arg) ;
                return true ;
             }
             else return false ;
           } // Handler

           public void handleBut(Event evt, Object arg) {
                String label = (String)arg ;
                layout.show(out, "first")  ;
                con.obt1.setBackground(Color.yellow) ;
                con.obt2.setBackground(Color.white) ;
                con.obt3.setBackground(Color.white) ;
                con.obt4.setBackground(Color.white) ;
                con.obt5.setBackground(Color.white) ;
                outopt = 0 ;
                if(label.equals("Pressure")) {
                   dispp = 0 ;
                   pl1.setBackground(Color.yellow) ;
                   pl2.setBackground(Color.white) ;
                   pl3.setBackground(Color.white) ;
                   pl4.setBackground(Color.white) ;
                   l.f.pl3.setBackground(Color.white) ;
                   l.f.pl3.setForeground(Color.red) ;
                   l.f.pl4.setBackground(Color.white) ;
                   l.f.pl4.setForeground(Color.red) ;
                   l.f.pl5.setBackground(Color.white) ;
                   l.f.pl5.setForeground(Color.red) ;
                   l.f.pl6.setBackground(Color.white) ;
                   l.f.pl6.setForeground(Color.red) ;
                   l.f.pl7.setBackground(Color.white) ;
                   l.f.pl7.setForeground(Color.red) ;
                   l.f.pl8.setBackground(Color.white) ;
                   l.f.pl8.setForeground(Color.red) ;
                   l.f.pl9.setBackground(Color.white) ;
                   l.f.pl9.setForeground(Color.red) ;
                }
                if(label.equals("Velocity")) {
                   dispp = 1 ;
                   pl1.setBackground(Color.white) ;
                   pl2.setBackground(Color.yellow) ;
                   pl3.setBackground(Color.white) ;
                   pl4.setBackground(Color.white) ;
                   l.f.pl3.setBackground(Color.white) ;
                   l.f.pl3.setForeground(Color.red) ;
                   l.f.pl4.setBackground(Color.white) ;
                   l.f.pl4.setForeground(Color.red) ;
                   l.f.pl5.setBackground(Color.white) ;
                   l.f.pl5.setForeground(Color.red) ;
                   l.f.pl6.setBackground(Color.white) ;
                   l.f.pl6.setForeground(Color.red) ;
                   l.f.pl7.setBackground(Color.white) ;
                   l.f.pl7.setForeground(Color.red) ;
                   l.f.pl8.setBackground(Color.white) ;
                   l.f.pl8.setForeground(Color.red) ;
                   l.f.pl9.setBackground(Color.white) ;
                   l.f.pl9.setForeground(Color.red) ;
                }
                if(label.equals("Drag Polar")) {
                   dispp = 9 ;
                   pl1.setBackground(Color.white) ;
                   pl2.setBackground(Color.white) ;
                   pl3.setBackground(Color.yellow) ;
                   pl4.setBackground(Color.white) ;
                   l.f.pl3.setBackground(Color.white) ;
                   l.f.pl3.setForeground(Color.red) ;
                   l.f.pl4.setBackground(Color.white) ;
                   l.f.pl4.setForeground(Color.red) ;
                   l.f.pl5.setBackground(Color.white) ;
                   l.f.pl5.setForeground(Color.red) ;
                   l.f.pl6.setBackground(Color.white) ;
                   l.f.pl6.setForeground(Color.red) ;
                   l.f.pl7.setBackground(Color.white) ;
                   l.f.pl7.setForeground(Color.red) ;
                   l.f.pl8.setBackground(Color.white) ;
                   l.f.pl8.setForeground(Color.red) ;
                   l.f.pl9.setBackground(Color.white) ;
                   l.f.pl9.setForeground(Color.red) ;
                }
                if(label.equals("Plane")) {
                   dispp = 25 ;
                   pl1.setBackground(Color.white) ;
                   pl2.setBackground(Color.white) ;
                   pl3.setBackground(Color.white) ;
                   pl4.setBackground(Color.yellow) ;
                   l.f.pl3.setBackground(Color.white) ;
                   l.f.pl3.setForeground(Color.red) ;
                   l.f.pl4.setBackground(Color.white) ;
                   l.f.pl4.setForeground(Color.red) ;
                   l.f.pl5.setBackground(Color.white) ;
                   l.f.pl5.setForeground(Color.red) ;
                   l.f.pl6.setBackground(Color.white) ;
                   l.f.pl6.setForeground(Color.red) ;
                   l.f.pl7.setBackground(Color.white) ;
                   l.f.pl7.setForeground(Color.red) ;
                   l.f.pl8.setBackground(Color.white) ;
                   l.f.pl8.setForeground(Color.red) ;
                   l.f.pl9.setBackground(Color.white) ;
                   l.f.pl9.setForeground(Color.red) ;
                }

                calcrange = 0 ;
                computeFlow() ;
              } // end handlebut
        } // Upper

        class L extends Panel {
           Foil outerparent ;
           F f ;
           C c ;

           L (Foil target) {
              outerparent = target ;
              layplt = new CardLayout() ;
              setLayout(layplt) ;

              f = new F(outerparent) ;
              c = new C(outerparent) ;

              add ("first", f) ;
              add ("second", c) ;
           }

           class F extends Panel {
              Foil outerparent ;
              Label l2 ;
              Button pl3,pl4,pl5,pl6,pl7,pl8,pl9 ;
              Choice plout,ploutb  ;
    
              F (Foil target) {
                 outerparent = target ;
                 setLayout(new GridLayout(3,4,5,5)) ;

                 ploutb = new Choice() ;
                 ploutb.addItem("Lift vs.") ;
                 ploutb.addItem("Drag vs.");
                 ploutb.setBackground(Color.white) ;
                 ploutb.setForeground(Color.red) ;
                 ploutb.select(0) ;

                 plout = new Choice() ;
                 plout.addItem("Lift vs.") ;
                 plout.addItem("Cl vs.");
                 plout.addItem("Drag vs.");
                 plout.addItem("Cd vs.");
                 plout.setBackground(Color.white) ;
                 plout.setForeground(Color.red) ;
                 plout.select(0) ;
     
                 pl3 = new Button("Angle") ;
                 pl3.setBackground(Color.white) ;
                 pl3.setForeground(Color.red) ;

                 pl4 = new Button("Thickness") ;
                 pl4.setBackground(Color.white) ;
                 pl4.setForeground(Color.red) ;

                 pl5 = new Button("Camber") ;
                 pl5.setBackground(Color.white) ;
                 pl5.setForeground(Color.red) ;

                 pl6 = new Button("Speed") ;
                 pl6.setBackground(Color.white) ;
                 pl6.setForeground(Color.red) ;

                 pl7 = new Button("Altitude") ;
                 pl7.setBackground(Color.white) ;
                 pl7.setForeground(Color.red) ;

                 pl8 = new Button("Wing Area") ;
                 pl8.setBackground(Color.white) ;
                 pl8.setForeground(Color.red) ;

                 pl9 = new Button("Density") ;
                 pl9.setBackground(Color.white) ;
                 pl9.setForeground(Color.red) ;

                 add(plout) ;
                 add(pl3) ;
                 add(pl5) ;
                 add(pl4) ;
   
                 add(ploutb) ;
                 add(pl6) ;
                 add(pl7) ;
                 add(new Label(" ", Label.RIGHT)) ;
   
                 add(new Label(" ", Label.RIGHT)) ;
                 add(pl8) ;
                 add(pl9) ;
                 add(new Label(" ", Label.RIGHT)) ;
              }

              public boolean action(Event evt, Object arg) {
                if(evt.target instanceof Button) {
                   handleBut(evt,arg) ;
                   return true ;
                }
                if(evt.target instanceof Choice) {
                   String label = (String)arg ;
                   dout = plout.getSelectedIndex() ;
                   doutb = ploutb.getSelectedIndex() ;
                    
                   computeFlow() ;
                   return true ;
                }
                else return false ;
              } // Handler

              public void handleBut(Event evt, Object arg) {
                   String label = (String)arg ;
                   layout.show(out, "first")  ;
                   con.obt1.setBackground(Color.yellow) ;
                   con.obt2.setBackground(Color.white) ;
                   con.obt3.setBackground(Color.white) ;
                   con.obt4.setBackground(Color.white) ;
                   con.obt5.setBackground(Color.white) ;
                   outopt = 0 ;
                   if(label.equals("Angle")) {
                      dispp = 2 ;
                      pl3.setBackground(Color.yellow) ;
                      pl3.setForeground(Color.black) ;
                      pl4.setBackground(Color.white) ;
                      pl4.setForeground(Color.red) ;
                      pl5.setBackground(Color.white) ;
                      pl5.setForeground(Color.red) ;
                      pl6.setBackground(Color.white) ;
                      pl6.setForeground(Color.red) ;
                      pl7.setBackground(Color.white) ;
                      pl7.setForeground(Color.red) ;
                      pl8.setBackground(Color.white) ;
                      pl8.setForeground(Color.red) ;
                      pl9.setBackground(Color.white) ;
                      pl9.setForeground(Color.red) ;
                      u.pl1.setBackground(Color.white) ;
                      u.pl2.setBackground(Color.white) ;
                      u.pl3.setBackground(Color.white) ;
                   }
                   if(label.equals("Thickness")) {
                      dispp = 3 ;
                      pl3.setBackground(Color.white) ;
                      pl3.setForeground(Color.red) ;
                      pl4.setBackground(Color.yellow) ;
                      pl4.setForeground(Color.black) ;
                      pl5.setBackground(Color.white) ;
                      pl5.setForeground(Color.red) ;
                      pl6.setBackground(Color.white) ;
                      pl6.setForeground(Color.red) ;
                      pl7.setBackground(Color.white) ;
                      pl7.setForeground(Color.red) ;
                      pl8.setBackground(Color.white) ;
                      pl8.setForeground(Color.red) ;
                      pl9.setBackground(Color.white) ;
                      pl9.setForeground(Color.red) ;
                      u.pl1.setBackground(Color.white) ;
                      u.pl2.setBackground(Color.white) ;
                      u.pl3.setBackground(Color.white) ;
                   }
                   if(label.equals("Camber")) {
                      dispp = 4 ;
                      pl3.setBackground(Color.white) ;
                      pl3.setForeground(Color.red) ;
                      pl4.setBackground(Color.white) ;
                      pl4.setForeground(Color.red) ;
                      pl5.setBackground(Color.yellow) ;
                      pl5.setForeground(Color.black) ;
                      pl6.setBackground(Color.white) ;
                      pl6.setForeground(Color.red) ;
                      pl7.setBackground(Color.white) ;
                      pl7.setForeground(Color.red) ;
                      pl8.setBackground(Color.white) ;
                      pl8.setForeground(Color.red) ;
                      pl9.setBackground(Color.white) ;
                      pl9.setForeground(Color.red) ;
                      u.pl1.setBackground(Color.white) ;
                      u.pl3.setBackground(Color.white) ;
                      u.pl2.setBackground(Color.white) ;
                   }
                   if(label.equals("Speed")) {
                      dispp = 5 ;
                      pl3.setBackground(Color.white) ;
                      pl3.setForeground(Color.red) ;
                      pl4.setBackground(Color.white) ;
                      pl4.setForeground(Color.red) ;
                      pl5.setBackground(Color.white) ;
                      pl5.setForeground(Color.red) ;
                      pl6.setBackground(Color.yellow) ;
                      pl6.setForeground(Color.black) ;
                      pl7.setBackground(Color.white) ;
                      pl7.setForeground(Color.red) ;
                      pl8.setBackground(Color.white) ;
                      pl8.setForeground(Color.red) ;
                      pl9.setBackground(Color.white) ;
                      pl9.setForeground(Color.red) ;
                      u.pl1.setBackground(Color.white) ;
                      u.pl2.setBackground(Color.white) ;
                      u.pl3.setBackground(Color.white) ;
                   }
                   if(label.equals("Altitude")) {
                      dispp = 6 ;
                      pl3.setBackground(Color.white) ;
                      pl3.setForeground(Color.red) ;
                      pl4.setBackground(Color.white) ;
                      pl4.setForeground(Color.red) ;
                      pl5.setBackground(Color.white) ;
                      pl5.setForeground(Color.red) ;
                      pl6.setBackground(Color.white) ;
                      pl6.setForeground(Color.red) ;
                      pl7.setBackground(Color.yellow) ;
                      pl7.setForeground(Color.black) ;
                      pl8.setBackground(Color.white) ;
                      pl8.setForeground(Color.red) ;
                      pl9.setBackground(Color.white) ;
                      pl9.setForeground(Color.red) ;
                      u.pl1.setBackground(Color.white) ;
                      u.pl2.setBackground(Color.white) ;
                      u.pl3.setBackground(Color.white) ;
                   }
                   if(label.equals("Wing Area")) {
                      dispp = 7 ;
                      pl3.setBackground(Color.white) ;
                      pl3.setForeground(Color.red) ;
                      pl4.setBackground(Color.white) ;
                      pl4.setForeground(Color.red) ;
                      pl5.setBackground(Color.white) ;
                      pl5.setForeground(Color.red) ;
                      pl6.setBackground(Color.white) ;
                      pl6.setForeground(Color.red) ;
                      pl7.setBackground(Color.white) ;
                      pl7.setForeground(Color.red) ;
                      pl8.setBackground(Color.yellow) ;
                      pl8.setForeground(Color.black) ;
                      pl9.setBackground(Color.white) ;
                      pl9.setForeground(Color.red) ;
                      u.pl1.setBackground(Color.white) ;
                      u.pl2.setBackground(Color.white) ;
                      u.pl3.setBackground(Color.white) ;
                   }
                   if(label.equals("Density")) {
                       dispp = 8 ;
                      pl3.setBackground(Color.white) ;
                      pl3.setForeground(Color.red) ;
                      pl4.setBackground(Color.white) ;
                      pl4.setForeground(Color.red) ;
                      pl5.setBackground(Color.white) ;
                      pl5.setForeground(Color.red) ;
                      pl6.setBackground(Color.white) ;
                      pl6.setForeground(Color.red) ;
                      pl7.setBackground(Color.white) ;
                      pl7.setForeground(Color.red) ;
                      pl8.setBackground(Color.white) ;
                      pl8.setForeground(Color.red) ;
                      pl9.setBackground(Color.yellow) ;
                      pl9.setForeground(Color.black) ;
                      u.pl1.setBackground(Color.white) ;
                      u.pl2.setBackground(Color.white) ;
                      u.pl3.setBackground(Color.white) ;
                   }
    
                   computeFlow() ;
              }

           }  // foil

           class C extends Panel {
              Foil outerparent ;
              Label l2 ;
   
              C (Foil target) {
                 outerparent = target ;
                 setLayout(new GridLayout(1,1,5,5)) ;

                 l2 = new Label(" ", Label.RIGHT) ;

                 add(l2) ;
              }
           }  // cyl
        } // Lower
     }  // Grf

     class Anl extends Panel {
        Foil outerparent ;
        Label l1,l2,l3,l4,l5,l6,l7 ;
        Button bt1,bt2,bt3,bt4,bt5,bt6,bt7,bt8,bt9,bt10,bt11,bt12 ;
        Button cbt1,cbt2,cbt3 ;

        Anl (Foil target) {

           outerparent = target ;
           setLayout(new GridLayout(7,3,5,5)) ;

           l2 = new Label("Units:", Label.RIGHT) ;  
           bt1 = new Button("Imperial Units") ;
           bt1.setBackground(Color.yellow) ;
           bt1.setForeground(Color.blue) ;

           bt2 = new Button("Metric Units") ;
           bt2.setBackground(Color.white) ;
           bt2.setForeground(Color.blue) ;
 
           l1 = new Label("Lift Analysis", Label.RIGHT) ;
           bt3 = new Button("Ideal Flow") ;
           bt3.setBackground(Color.white) ;
           bt3.setForeground(Color.blue) ;

           bt4 = new Button("Stall Model") ;
           bt4.setBackground(Color.yellow) ;
           bt4.setForeground(Color.blue) ;

           l3 = new Label("AR Lift Correction:", Label.RIGHT) ;
           bt5 = new Button("AR On") ;
           bt5.setBackground(Color.yellow) ;
           bt5.setForeground(Color.blue) ;

           bt6 = new Button("AR Off") ;
           bt6.setBackground(Color.white) ;
           bt6.setForeground(Color.blue) ;

           l4 = new Label("Induced Drag:", Label.RIGHT) ;
           bt7 = new Button("ID On") ;
           bt7.setBackground(Color.yellow) ;
           bt7.setForeground(Color.blue) ;

           bt8 = new Button("ID Off") ;
           bt8.setBackground(Color.white) ;
           bt8.setForeground(Color.blue) ;

           l5 = new Label("Re # Correction:", Label.RIGHT) ;
           bt9 = new Button("Re On") ;
           bt9.setBackground(Color.yellow) ;
           bt9.setForeground(Color.blue) ;

           bt10 = new Button("Re Off") ;
           bt10.setBackground(Color.white) ;
           bt10.setForeground(Color.blue) ;

           l7 = new Label("Kutta Condition", Label.RIGHT) ;
           bt11 = new Button("Kut On") ;
           bt11.setBackground(Color.yellow) ;
           bt11.setForeground(Color.blue) ;

           bt12 = new Button("Kut Off") ;
           bt12.setBackground(Color.white) ;
           bt12.setForeground(Color.blue) ;

           l6= new Label("Drag of Ball", Label.CENTER) ;
           l6.setForeground(Color.red) ;       
           cbt1 = new Button("Smooth Ball") ;
           cbt1.setBackground(Color.white) ;
           cbt1.setForeground(Color.red) ;

           cbt2 = new Button("Rough Ball") ;
           cbt2.setBackground(Color.white) ;
           cbt2.setForeground(Color.red) ;

           cbt3 = new Button("Golf Ball") ;
           cbt3.setBackground(Color.white) ;
           cbt3.setForeground(Color.red) ;

           add(l1) ;
           add(bt4) ;
           add(bt3) ;

           add(l7) ;
           add(bt11) ;
           add(bt12) ;

           add(l3) ;
           add(bt5) ;
           add(bt6) ;

           add(l4) ;
           add(bt7) ;
           add(bt8) ;

           add(l5) ;
           add(bt9) ;
           add(bt10) ;

 //          add(bt1) ;
           add(l6) ; 
           add(cbt1) ;
           add(cbt2) ;

 //          add(bt2) ;
           add(new Label(" ", Label.CENTER)) ;
           add(cbt3) ;
           add(new Label(" ", Label.CENTER)) ;
        }

        public boolean action(Event evt, Object arg) {
           if(evt.target instanceof Button) {
              handleBut(evt,arg) ;
              return true ;
           }
           else return false ;
        } // Handler

        public void handleBut(Event evt, Object arg) {
             String label = (String)arg ;
           
             if(label.equals("Imperial Units")) {
                 lunits = 0 ;
                 bt1.setBackground(Color.yellow) ;
                 bt2.setBackground(Color.white) ;
                 setUnits () ;
             }
             if(label.equals("Metric Units")) {
                 lunits = 1 ;
                 bt1.setBackground(Color.white) ;
                 bt2.setBackground(Color.yellow) ;
                 setUnits () ;
             }
             if(label.equals("Ideal Flow")) {
                 anflag = 0 ;
                 bt3.setBackground(Color.yellow) ;
                 bt4.setBackground(Color.white) ;
             }
             if(label.equals("Stall Model")) {
                 anflag = 1 ;
                 bt3.setBackground(Color.white) ;
                 bt4.setBackground(Color.yellow) ;
             }
             if(label.equals("Kut On")) {
                 anflag = 0 ;
                 bt12.setBackground(Color.white) ;
                 bt11.setBackground(Color.yellow) ;
             }
             if(label.equals("Kut Off")) {
                 anflag = 2 ;
                 bt11.setBackground(Color.white) ;
                 bt12.setBackground(Color.yellow) ;
             }

             if(label.equals("AR On")) {
                 arcor = 1 ;
                 bt6.setBackground(Color.white) ;
                 bt5.setBackground(Color.yellow) ;
             }
             if(label.equals("AR Off")) {
                 arcor = 0 ;
                 bt5.setBackground(Color.white) ;
                 bt6.setBackground(Color.yellow) ;
             }
             if(label.equals("ID On")) {
                 indrag = 1 ;
                 bt8.setBackground(Color.white) ;
                 bt7.setBackground(Color.yellow) ;
             }
             if(label.equals("ID Off")) {
                 indrag = 0 ;
                 bt7.setBackground(Color.white) ;
                 bt8.setBackground(Color.yellow) ;
             }
             if(label.equals("Re On")) {
                 recor = 1 ;
                 bt10.setBackground(Color.white) ;
                 bt9.setBackground(Color.yellow) ;
             }
             if(label.equals("Re Off")) {
                 recor = 0 ;
                 bt9.setBackground(Color.white) ;
                 bt10.setBackground(Color.yellow) ;
             }
             if(label.equals("Smooth Ball")) {
                 bdragflag = 1 ;
                 cbt1.setBackground(Color.yellow) ;
                 cbt2.setBackground(Color.white) ;
                 cbt3.setBackground(Color.white) ;
             }
             if(label.equals("Rough Ball")) {
                 bdragflag = 2 ;
                 cbt2.setBackground(Color.yellow) ;
                 cbt1.setBackground(Color.white) ;
                 cbt3.setBackground(Color.white) ;
             }
             if(label.equals("Golf Ball")) {
                 bdragflag = 3 ;
                 cbt3.setBackground(Color.yellow) ;
                 cbt2.setBackground(Color.white) ;
                 cbt1.setBackground(Color.white) ;
             }

             loadInput() ;
         }
     } // end Anl

     class Genp extends Panel {
        Foil outerparent ;
        Inl inl ;
        Inr inr ;

        Genp (Foil target) {

           outerparent = target ;
           setLayout(new GridLayout(1,2,5,5)) ;

           inl = new Inl(outerparent) ;
           inr = new Inr(outerparent) ;

           add(inl) ;
           add(inr) ;
        }

        class Inl extends Panel {
           Foil outerparent ;
           TextField f1,f2,f3,f4 ;
           Label l1,l2,l3,l4 ;
           Label l01,l02 ;

           Inl (Foil target) {

            outerparent = target ;
            setLayout(new GridLayout(5,2,2,10)) ;

            l01 = new Label("Generating", Label.RIGHT) ;
            l01.setForeground(Color.blue) ;
            l02 = new Label("Cylinder", Label.LEFT) ;
            l02.setForeground(Color.blue) ;

            l1 = new Label("Radius", Label.CENTER) ;
            f1 = new TextField("1.0",5) ;

            l2 = new Label("X-val", Label.CENTER) ;
            f2 = new TextField("0.0",5) ;

            l3 = new Label("Y-val", Label.CENTER) ;
            f3 = new TextField("0.0",5) ;

            l4 = new Label("Circulation", Label.CENTER) ;
            f4 = new TextField("0.0",5) ;

            add(l01) ;
            add(l02) ;

            add(l1) ;
            add(f1) ;

            add(l2) ;
            add(f2) ;

            add(l3) ;
            add(f3) ;

            add(l4) ;
            add(f4) ;
          }

          public boolean handleEvent(Event evt) {
            Double V1,V2,V3,V4 ;
            double v1,v2,v3,v4 ;
            float fl1 ;
            int i1,i2,i3,i4 ;

            if(evt.id == Event.ACTION_EVENT) {
              V1 = Double.valueOf(f1.getText()) ;
              v1 = V1.doubleValue() ;
              V2 = Double.valueOf(f2.getText()) ;
              v2 = V2.doubleValue() ;
              V3 = Double.valueOf(f3.getText()) ;
              v3 = V3.doubleValue() ;
              V4 = Double.valueOf(f4.getText()) ;
              v4 = V4.doubleValue() ;

              rval = v1 ;
              if(v1 < 1.0) {
                rval = v1 = 1.0 ;
                fl1 = (float) v1 ;
                f1.setText(String.valueOf(fl1)) ;
              }
              if(v1 > 5.0) {
                rval = v1 = 5.0 ;
                fl1 = (float) v1 ;
                f1.setText(String.valueOf(fl1)) ;
              }

              xcval = v2 ;
              if(v2 < -1.0) {
                xcval = v2 = -1.0 ;
                fl1 = (float) v2 ;
                f2.setText(String.valueOf(fl1)) ;
              }
              if(v2 > 1.0) {
                xcval = v2 = 1.0 ;
                fl1 = (float) v2 ;
                f2.setText(String.valueOf(fl1)) ;
              }

              ycval = v3 ;
              if(v3 < -1.0) {
                ycval = v3 = -1.0  ;
                fl1 = (float) v3 ;
                f3.setText(String.valueOf(fl1)) ;
              }
              if(v3 > 1.0) {
                ycval = v3 = 1.0 ;
                fl1 = (float) v3 ;
                f3.setText(String.valueOf(fl1)) ;
              }

              gamval = v4 ;
              if(v4 < -2.0) {
                gamval = v4 = -2.0  ;
                fl1 = (float) v4 ;
                f4.setText(String.valueOf(fl1)) ;
              }
              if(v4 > 2.0) {
                gamval = v4 = 2.0 ;
                fl1 = (float) v4 ;
                f4.setText(String.valueOf(fl1)) ;
              }

              i1 = (int) (((v1 - 1.0)/(4.0))*1000.) ;
              i2 = (int) (((v2 + 1.0)/(2.0))*1000.) ;
              i3 = (int) (((v3 + 1.0)/(2.0))*1000.) ;
              i4 = (int) (((v4 + 2.0)/(4.0))*1000.) ;

              inr.s1.setValue(i1) ;
              inr.s2.setValue(i2) ;
              inr.s3.setValue(i3) ;
              inr.s4.setValue(i4) ;

              computeFlow() ;
              return true ;
            }
            else return false ;
          } // Handler
        }  // Inl

        class Inr extends Panel {
           Foil outerparent ;
           Scrollbar s1,s2,s3,s4;

           Inr (Foil target) {
            int i1,i2,i3,i4 ;

            outerparent = target ;
            setLayout(new GridLayout(5,1,2,10)) ;

            i1 = (int) (((rval - 1.0)/(4.0))*1000.) ;
            i2 = (int) (((xcval + 1.0)/(2.0))*1000.) ;
            i3 = (int) (((ycval + 1.0)/(2.0))*1000.) ;
            i4 = (int) (((gamval + 2.0)/(4.0))*1000.) ;

            s1 = new Scrollbar(Scrollbar.HORIZONTAL,i1,10,0,1000);
            s2 = new Scrollbar(Scrollbar.HORIZONTAL,i2,10,0,1000);
            s3 = new Scrollbar(Scrollbar.HORIZONTAL,i3,10,0,1000);
            s4 = new Scrollbar(Scrollbar.HORIZONTAL,i4,10,0,1000);

            add(new Label(" ", Label.CENTER)) ;
            add(s1) ;
            add(s2) ;
            add(s3) ;
            add(s4) ;
          }


          public boolean handleEvent(Event evt) {
               if(evt.id == Event.SCROLL_ABSOLUTE) {
                  this.handleBar(evt) ;
                  return true ;
               }
               if(evt.id == Event.SCROLL_LINE_DOWN) {
                  this.handleBar(evt) ;
                  return true ;
               }
               if(evt.id == Event.SCROLL_LINE_UP) {
                  this.handleBar(evt) ;
                  return true ;
               }
               if(evt.id == Event.SCROLL_PAGE_DOWN) {
                  this.handleBar(evt) ;
                  return true ;
               }
               if(evt.id == Event.SCROLL_PAGE_UP) {
                  this.handleBar(evt) ;
                  return true ;
               }
               else return false ;
          }

          public void handleBar(Event evt) {
             int i1,i2,i3,i4 ;
             double v1,v2,v3,v4 ;
             float fl1,fl2,fl3,fl4 ;

    // Input for computations
             i1 = s1.getValue() ;
             i2 = s2.getValue() ;
             i3 = s3.getValue() ;
             i4 = s4.getValue() ;

             rval = v1 = i1 * (4.0)/ 1000. + 1.0 ;
             xcval = v2 = i2 * (2.0)/ 1000. - 1.0 ;
             ycval = v3 = i3 * (2.0)/ 1000. - 1.0 ;
             gamval = v4 = i4 * (4.0)/ 1000. - 2.0 ;

             fl1 = (float) v1 ;
             fl2 = (float) v2 ;
             fl3 = (float) v3 ;
             fl4 = (float) v4 ;

             inl.f1.setText(String.valueOf(fl1)) ;
             inl.f2.setText(String.valueOf(fl2)) ;
             inl.f3.setText(String.valueOf(fl3)) ;
             inl.f4.setText(String.valueOf(fl4)) ;

             computeFlow() ;
           }
        }  // Inr
     }  // Genp
  }  // In 

  class Out extends Panel {
     Foil outerparent ;
     Plt plt ;
     Prb prb ;
     Perf perf ;

     Out (Foil target) { 
        outerparent = target ;
        layout = new CardLayout() ;
        setLayout(layout) ;

        plt = new Plt(outerparent) ;
        prb = new Prb(outerparent) ;
        perf = new Perf(outerparent) ;

        add ("first", plt) ;
        add ("second", prb) ;
        add ("third", perf) ;
     }
 

     class Plt extends Canvas
         implements Runnable{
        Foil outerparent ;
        Thread run2 ;
        Point locp,ancp;

        Plt (Foil target) { 
           setBackground(Color.blue) ;
           run2 = null ;
        }
   
        public boolean mouseUp(Event evt, int x, int y) {
           handleb(x,y) ;
           return true;                                        
        }

        public void handleb(int x, int y) {
            if (y >= 185) { 
               if (x >= 5 && x <= 55) {   //rescale
                   endy = 0.0 ;
                   begy = 0.0 ;
                   calcrange = 0 ;
                   computeFlow() ;
               }
            }
            out.plt.repaint() ;
        }

        public void start() {
           if (run2 == null) {
              run2 = new Thread(this) ;
              run2.start() ;
           }
        }

        public void run() {
          int timer ;
 
          timer = 100 ;
          while (true) {
             try { Thread.sleep(timer); }
             catch (InterruptedException e) {}
             out.plt.repaint() ;
          }
        }

        public void loadPlot() {
          double rad,ang,xc,yc,lftref,clref,drgref,cdref ;
          double del,spd,awng,ppl,tpl,hpl,angl,thkpl,campl,clpl,cdpl ;
          int index,ic ;

          lines = 1 ;
          clref =  getClplot(camval,thkval,alfval) ;
          if (Math.abs(clref) <= .001) clref = .001 ;    /* protection */
          lftref = clref * q0 * area/lconv/lconv ;
          alfd = alfval ;
          thkd = thkinpt ;
          camd = caminpt ;
//   attempt to fix symmetry problem
    if (camd < 0.0) alfd = - alfval ;
//
          solve.getDrag(clref) ;
          cdref = dragco ;
          drgref = cdref * q0 * area/lconv/lconv ;
   
// load up the view image
          for (ic = 0; ic <= nlnc; ++ ic) {
             for (index = 0; index <= nptc; ++ index) {
                if (foil <= 3) {
                   xpl[ic][index] = xm[ic][index] ;
                   ypl[ic][index] = ym[ic][index] ;
                }
                if (foil >= 4) {
                   xpl[ic][index] = xg[ic][index] ;
                   ypl[ic][index] = yg[ic][index] ;
                }
             }
          }

// load up the generating plane
          if (dispp == 25) {
             for (ic = 0; ic <= nlnc; ++ ic) {
                for (index = 0; index <= nptc; ++ index) {
                   xplg[ic][index] = xgc[ic][index] ;
                   yplg[ic][index] = ygc[ic][index] ;
                }
             }
          }

// probe
          for (index = 0; index <= nptc; ++ index) {
             if (foil <= 3) {
                xpl[19][index] = xm[19][index] ;
                ypl[19][index] = ym[19][index] ;
                pxpl = pxm ;
                pypl = pym ;
             }
             if (foil >= 4) {
                xpl[19][index] = xg[19][index] ;
                ypl[19][index] = yg[19][index] ;
                pxpl = pxg ;
                pypl = pyg ;
             }
          }

//  load up surface plots

          if (dispp == 0) {    // pressure variation
              npt = npt2 ;
              ntr = 3 ;
              nord = nabs = 1 ;
              for (index = 1; index <= npt; ++ index) {
                  if (foil <= 3) {
                     pltx[0][index] =100.*(xpl[0][npt2-index + 1]/4.0 + .5) ;
                     pltx[1][index] =100.*(xpl[0][npt2+index - 1]/4.0 + .5) ;
                     pltx[2][index] =100.*(xpl[0][npt2+index - 1]/4.0 + .5) ;
                  }
                  if (foil >= 4) {
                     pltx[0][index]=100.*(xpl[0][npt2-index+1]/(2.0*radius/lconv)+.5);
                     pltx[1][index]=100.*(xpl[0][npt2+index-1]/(2.0*radius/lconv)+.5);
                     pltx[2][index]=100.*(xpl[0][npt2+index-1]/(2.0*radius/lconv)+.5);
                  }
                  plty[0][index] = plp[npt2-index + 1] ;
                  plty[1][index] = plp[npt2+index - 1] ;
                  plty[2][index] = ps0/2116. * pconv ;
// **** Impose pstatic on surface plot for stalled foil
                  if (anflag ==1 && index > 7) {
                     if (alfval >  10.0) plty[0][index] = plty[2][index] ;
                     if (alfval < -10.0) plty[1][index] = plty[2][index] ;
                  }
// *******
              }
              begx = 0.0 ;
              endx = 100. ;
              ntikx = 5 ;
              ntiky = 5 ;
       //       endy=1.02 * ps0/2116. * pconv ;
       //       begy=.95 * ps0/2116. * pconv ;
              laby = String.valueOf("Press");
              if (lunits == 0) labyu = String.valueOf("psi");
              if (lunits == 1) labyu = String.valueOf("k-Pa");
              labx = String.valueOf(" X ");
              if (foil <= 3) labxu = String.valueOf("% chord");
              if (foil >= 4) labxu = String.valueOf("% diameter");
          }
          if (dispp == 1) {    // velocity variation
              npt = npt2 ;
              ntr = 3 ;
              nord = 2 ;
              nabs = 1 ;
              for (index = 1; index <= npt; ++ index) {
                  if (foil <= 3) {
                     pltx[0][index] = 100.*(xpl[0][npt2-index+1]/4.0+.5) ;
                     pltx[1][index] = 100.*(xpl[0][npt2+index-1]/4.0+.5) ;
                     pltx[2][index] = 100.*(xpl[0][npt2-index+1]/4.0+.5) ;
                  }
                  if (foil >= 4) {
                     pltx[0][index]=100.*(xpl[0][npt2-index+1]/(2.0*radius/lconv)+.5);
                     pltx[1][index]=100.*(xpl[0][npt2+index-1]/(2.0*radius/lconv)+.5);
                     pltx[2][index]=100.*(xpl[0][npt2+index-1]/(2.0*radius/lconv)+.5);
                  }
                  plty[0][index] = plv[npt2-index+1];
                  plty[1][index] = plv[npt2+index-1] ;
                  plty[2][index] = vfsd ;
// **** Impose free stream vel on surface plot for stalled foil
                  if (anflag ==1 && index > 7) {
                     if (alfval >  10.0) plty[0][index] = plty[2][index] ;
                     if (alfval < -10.0) plty[1][index] = plty[2][index] ;
                  }
// *******
              }
              begx = 0.0 ;
              endx = 100. ;
              ntikx = 5 ;
              ntiky = 6 ;
        //      begy = 0.0 ;
        //      endy = 500. ;
              laby = String.valueOf("Vel");
              if (lunits == 0) labyu = String.valueOf("mph");
              if (lunits == 1) labyu = String.valueOf("kmh");
              labx = String.valueOf(" X ");
              if (foil <= 3) labxu = String.valueOf("% chord");
              if (foil >= 4) labxu = String.valueOf("% diameter");
          }

//  load up performance plots

          if (dispp == 2) {    // lift or drag versus angle
              npt = 21 ;
              ntr = 1 ;
              nabs = 2;  nord = 3 ;
              begx=-20.0; endx=20.0; ntikx=5;
              labx = String.valueOf("Angle ");
              labxu = String.valueOf("degrees");
              del = 40.0 / (npt-1) ;
              for (ic=1; ic <=npt; ++ic) {
                   angl = -20.0 + (ic-1)*del ;
                   clpl = getClplot(camval,thkval,angl) ;
                   alfd = angl ;
                   thkd = thkinpt ;
                   camd = caminpt ;

//   attempt to fix symmetry problem
    if (camd < 0.0) alfd = - angl ;
//
                   solve.getDrag(clpl) ;
                   cdpl = dragco ;

                   if ( dout <= 1) {
                      pltx[0][ic] = angl ;
                      if (dout == 0)plty[0][ic] = fconv*lftref * clpl/clref ;
                      if (dout == 1)plty[0][ic] = 100.*clpl ;
                   }
                   else {
                      pltx[0][ic] = angl ;
                      if (dout == 2)plty[0][ic] = fconv*drgref * cdpl/cdref ;
                      if (dout == 3)plty[0][ic] = 100.*cdpl ;
                   }
              }
              ntiky = 5 ;
              pltx[1][0] = alfval ;
              if (dout == 0) {
                  laby = String.valueOf("Lift");
                  if (lunits == 0) labyu = String.valueOf("lbs");
                  if (lunits == 1) labyu = String.valueOf("N");
                  plty[1][0] = lftref*fconv ;
              }
              if (dout == 1) {
                  laby = String.valueOf("Cl");
                  labyu = String.valueOf("x 100 ");
                  plty[1][0] = 100.*clift ;
              }
              if (dout == 2) {
                  laby = String.valueOf("Drag");
                  if (lunits == 0) labyu = String.valueOf("lbs");
                  if (lunits == 1) labyu = String.valueOf("N");
                  plty[1][0] = drgref*fconv ;
              }
              if (dout == 3) {
                  laby = String.valueOf("Cd");
                  labyu = String.valueOf("x 100 ");
                  plty[1][0] = 100.*dragCoeff ;
              }              
          }

          if (dispp == 3) {    // lift or drag versus thickness
              npt = 20 ;
              ntr = 1 ;
              nabs = 3;  nord = 3 ;
              begx=0.0; endx=20.0; ntikx=6;
              labx = String.valueOf("Thickness ");
              labxu = String.valueOf("% chord");
              del = 1.0 / npt ;
              for (ic=1; ic <=npt; ++ic) {
                   thkpl = .05 + (ic-1)*del ;
                   clpl = getClplot(camval,thkpl,alfval) ;
                   alfd = alfval ;
                   thkd = thkpl*25.0 ;
                   camd = caminpt ;
//   attempt to fix symmetry problem
     if (camd < 0.0) alfd = - alfval ;
//
                   solve.getDrag(clpl) ;
                   cdpl = dragco ;

                   if ( dout <= 1) {
                      pltx[0][ic] = thkpl*25. ;
                      if (dout == 0)plty[0][ic] = fconv*lftref * clpl/clref ;
                      if (dout == 1)plty[0][ic] = 100.*clpl ;
                   }
                   else {
                      pltx[0][ic] = thkd ;
                      if (dout == 2)plty[0][ic] = fconv*drgref * cdpl/cdref ;
                      if (dout == 3)plty[0][ic] = 100.*cdpl ;
                   }
              }
              ntiky = 5 ;
              pltx[1][0] = thkinpt ;
              if (dout == 0) {
                  laby = String.valueOf("Lift");
                  if (lunits == 0) labyu = String.valueOf("lbs");
                  if (lunits == 1) labyu = String.valueOf("N");
                  plty[1][0] = lftref*fconv ;
              }
              if (dout == 1) {
                  laby = String.valueOf("Cl");
                  labyu = String.valueOf("x 100 ");
                  plty[1][0] = 100.*clift ;
              }
              if (dout == 2) {
                  laby = String.valueOf("Drag");
                  if (lunits == 0) labyu = String.valueOf("lbs");
                  if (lunits == 1) labyu = String.valueOf("N");
                  plty[1][0] = drgref*fconv ;
                  plty[0][npt]= plty[0][npt-1]= plty[0][npt-2]=plty[0][npt-3]=plty[0][npt-4] ;
              }
              if (dout == 3) {
                  laby = String.valueOf("Cd");
                  labyu = String.valueOf("x 100 ");
                  plty[1][0] = 100.*dragCoeff ;
                  plty[0][npt]= plty[0][npt-1]= plty[0][npt-2]=plty[0][npt-3]=plty[0][npt-4] ;
              }
          }

          if (dispp == 4) {    // lift or drag versus camber
              npt = 21 ;
              ntr = 1 ;
              nabs = 4;  nord = 3 ;
              begx=-20.; endx=20.; ntikx=5;
              labx = String.valueOf("Camber ");
              labxu = String.valueOf("% chord");
              del = 2.0 / (npt-1) ;
              for (ic=1; ic <=npt; ++ic) {
                   campl = -1.0 + (ic-1)*del ;
                   clpl = getClplot(campl,thkval,alfval) ;
                   alfd = alfval ;
                   thkd = thkinpt ;
                   camd = campl * 25.0 ;
//   attempt to fix symmetry problem
     if (camd < 0.0) alfd = - alfval ;
//
                   solve.getDrag(clpl) ;
                   cdpl = dragco ;

                   if ( dout <= 1) {
                     pltx[0][ic] = campl*25.0 ;
                     if (dout == 0)plty[0][ic] = fconv*lftref * clpl/clref ;
                     if (dout == 1)plty[0][ic] = 100.*clpl ;
                   }
                   else {
                      pltx[0][ic] = camd  ;
                      if (dout == 2)plty[0][ic] = fconv*drgref * cdpl/cdref ;
                      if (dout == 3)plty[0][ic] = 100.*cdpl ;
                   }
              }
              ntiky = 5 ;
              pltx[1][0] = caminpt ;
              if (dout == 0) {
                  laby = String.valueOf("Lift");
                  if (lunits == 0) labyu = String.valueOf("lbs");
                  if (lunits == 1) labyu = String.valueOf("N");
                  plty[1][0] = lftref*fconv ;
              }
              if (dout == 1) {
                  laby = String.valueOf("Cl");
                  labyu = String.valueOf("x 100 ");
                  plty[1][0] = 100.*clift ;
              }
              if (dout == 2) {
                  laby = String.valueOf("Drag");
                  if (lunits == 0) labyu = String.valueOf("lbs");
                  if (lunits == 1) labyu = String.valueOf("N");
                  plty[1][0] = drgref*fconv ;
                  plty[0][1] = plty[0][2]= plty[0][3] ;
                  plty[0][npt] = plty[0][npt -1] = plty[0][npt - 2] ;
              }
              if (dout == 3) {
                  laby = String.valueOf("Cd");
                  labyu = String.valueOf("x 100 ");
                  plty[1][0] = 100.*dragCoeff ;
                  plty[0][1] = plty[0][2]= plty[0][3] ;
                  plty[0][npt] = plty[0][npt -1] = plty[0][npt-2] ;
              }
          }

          if (dispp == 5) {    // lift and drag versus speed
              npt = 20 ;
              ntr = 1 ;
              nabs = 5;  nord = 3 ;
              begx=0.0; endx=300.0; ntikx=7;
              labx = String.valueOf("Speed ");
              if (lunits == 0) labxu = String.valueOf("mph");
              if (lunits == 1) labxu = String.valueOf("kmh");
              del = vmax / npt ;
              for (ic=1; ic <=npt; ++ic) {
                  spd = (ic-1)*del ;
                  pltx[0][ic] = spd ;
                  if (doutb == 0) plty[0][ic] = fconv*lftref * spd * spd / (vfsd * vfsd) ;
                  if (doutb == 1) plty[0][ic] = fconv*drgref * spd * spd / (vfsd * vfsd) ;
              }
              ntiky = 5 ;
              if (doutb == 0) laby = String.valueOf("Lift");
              if (doutb == 1) laby = String.valueOf("Drag");
              pltx[1][0] = vfsd ;
              if (doutb == 0) plty[1][0] = lftref*fconv ;
              if (doutb == 1) plty[1][0] = drgref*fconv ;
              if (lunits == 0) labyu = String.valueOf("lbs");
              if (lunits == 1) labyu = String.valueOf("N");
          }

          if (dispp == 6) {    // lift and drag versus altitude
              npt = 20 ;
              ntr = 1 ;
              nabs = 6;  nord = 3 ;
              begx=0.0; endx=50.0; ntikx=6;
              if (lunits == 0) endx = 50.0 ;
              if (lunits == 1) endx = 15.0 ;
              labx = String.valueOf("Altitude");
              if (lunits == 0) labxu = String.valueOf("k-ft");
              if (lunits == 1) labxu = String.valueOf("km");
              del = altmax / npt ;
              for (ic=1; ic <=npt; ++ic) {
                  hpl = (ic-1)*del ;
                  pltx[0][ic] = lconv*hpl/1000. ;
                  tpl = 518.6 ;
                  ppl = 2116. ;
                  if (planet == 0) {
                      if (hpl < 36152.)   {
                            tpl = 518.6 - 3.56 * hpl /1000. ;
                            ppl = 2116. * Math.pow(tpl/518.6, 5.256) ;
                      }
                         else {
                            tpl = 389.98 ;
                            ppl = 2116. * .236 * Math.exp((36000.-hpl)/(53.35*tpl)) ;
                      }
                      if (doutb == 0) plty[0][ic] = fconv*lftref * ppl/(tpl*53.3*32.17) / rho ;
                      if (doutb == 1) plty[0][ic] = fconv*drgref * ppl/(tpl*53.3*32.17) / rho ;
                  }
                  if (planet == 1) {
                      if (hpl <= 22960.) {
                         tpl = 434.02 - .548 * hpl/1000. ;
                         ppl = 14.62 * Math.pow(2.71828,-.00003 * hpl) ;
                      }
                      if (hpl > 22960.) {
                         tpl = 449.36 - 1.217 * hpl/1000. ;
                         ppl = 14.62 * Math.pow(2.71828,-.00003 * hpl) ;
                      }
                      if (doutb == 0) plty[0][ic] = fconv*lftref * ppl/(tpl*1149.) / rho ;
                      if (doutb == 1) plty[0][ic] = fconv*drgref * ppl/(tpl*1149.) / rho ;
                  }
                  if (planet == 2) {
                      if (doutb == 0) plty[0][ic] = fconv*lftref ;
                      if (doutb == 1) plty[0][ic] = fconv*drgref ;
                  }
              }
              ntiky = 5 ;
              if (doutb == 0) laby = String.valueOf("Lift");
              if (doutb == 1) laby = String.valueOf("Drag");
              pltx[1][0] = alt/1000. ;
              if (doutb == 0) plty[1][0] = lftref*fconv ;
              if (doutb == 1) plty[1][0] = drgref*fconv ;
              if (lunits == 0) labyu = String.valueOf("lbs");
              if (lunits == 1) labyu = String.valueOf("N");
          }

          if (dispp == 7) {    // lift and drag versus area
              npt = 2 ;
              ntr = 1 ;
              nabs = 7;  nord = 3 ;
              begx=0.0; ntikx=6;
              labx = String.valueOf("Area ");
              if (lunits == 0) {
                  labxu = String.valueOf("sq ft");
                  endx = 2000.0 ;
                  labyu = String.valueOf("lbs");
                  pltx[0][1] = 0.0 ;
                  plty[0][1] = 0.0 ;
                  pltx[0][2] = 2000. ;
                  if (doutb == 0) plty[0][2] = fconv*lftref * 2000. /area ;
                  if (doutb == 1) plty[0][2] = fconv*drgref * 2000. /area ;
              }
              if (lunits == 1) {
                  labxu = String.valueOf("sq m");
                  endx = 200. ;
                  labyu = String.valueOf("N");
                  pltx[0][1] = 0.0 ;
                  plty[0][1] = 0.0 ;
                  pltx[0][2] = 200. ;
                  if (doutb == 0) plty[0][2] = fconv*lftref * 200. /area ; 
                  if (doutb == 1) plty[0][2] = fconv*drgref * 200. /area ; 
              }

              ntiky = 5 ;
              pltx[1][0] = area ;
              if (doutb == 0) {
                 laby = String.valueOf("Lift");
                 plty[1][0] = lftref*fconv ;
              }
              else {
                 laby = String.valueOf("Drag");
                 plty[1][0] = drgref*fconv ;
              }
          }

          if (dispp == 8) {    // lift and drag versus density
              npt = 2 ;
              ntr = 1 ;
              nabs = 7; nord = 3 ;
              begx=0.0; ntikx=6;
              labx = String.valueOf("Density ");
              if (planet == 0) {
                  if (lunits == 0) {
                      labxu = String.valueOf("x 10,000 slug/cu ft");
                      endx = 25.0 ;
                      pltx[0][1] = 0.0 ;
                      plty[0][1] = 0.0 ;
                      pltx[0][2] = 23.7 ;
                      if (doutb == 0) plty[0][2] = fconv*lftref * 23.7 /(rho*10000.);
                      if (doutb == 1) plty[0][2] = fconv*drgref * 23.7 /(rho*10000.);
                      pltx[1][0] = rho*10000. ;
                  }
                  if (lunits == 1) {
                      labxu = String.valueOf("g/cu m");
                      endx = 1250. ;
                      pltx[0][1] = 0.0 ;
                      plty[0][1] = 0.0 ;
                      pltx[0][2] = 1226 ;
                      if (doutb == 0) plty[0][2] = fconv*lftref * 23.7 /(rho*10000.);
                      if (doutb == 1) plty[0][2] = fconv*drgref * 23.7 /(rho*10000.);
                      pltx[1][0] = rho*1000.*515.4 ;
                  }
              }

              if (planet == 1) {
                  if (lunits == 0) {
                      labxu = String.valueOf("x 100,000 slug/cu ft");
                      endx = 5.0 ;
                      pltx[0][1] = 0.0 ;
                      plty[0][1] = 0.0 ;
                      pltx[0][2] = 2.93 ;
                      if (doutb == 0) plty[0][2] = fconv*lftref * 2.93 /(rho*100000.);
                      if (doutb == 1) plty[0][2] = fconv*drgref * 2.93 /(rho*100000.);
                      pltx[1][0] = rho*100000. ;
                  }
                  if (lunits == 1) {
                      labxu = String.valueOf("g/cu m");
                      endx = 15. ;
                      pltx[0][1] = 0.0 ;
                      plty[0][1] = 0.0 ;
                      pltx[0][2] = 15.1 ;
                      if (doutb == 0) plty[0][2] = fconv*lftref * 2.93 /(rho*100000.);
                      if (doutb == 1) plty[0][2] = fconv*drgref * 2.93 /(rho*100000.);
                      pltx[1][0] = rho*1000.*515.4 ;
                  }
              }
              ntiky = 5 ;
              if (doutb == 0) laby = String.valueOf("Lift");
              if (doutb == 1) laby = String.valueOf("Drag");
              if (doutb == 0) plty[1][0] = lftref*fconv ;
              if (doutb == 1) plty[1][0] = drgref*fconv ;
              if (lunits == 0) labyu = String.valueOf("lbs");
              if (lunits == 1) labyu = String.valueOf("N");
          }

          if (dispp == 9) {    // drag polar
              npt = 20 ;
              ntr = 1 ;
              nabs = 2;  nord = 3 ;
              ntikx=5;
              del = 40.0 / npt ;
              for (ic=1; ic <=npt; ++ic) {
                   angl = -20.0 + (ic-1)*del ;
                   clpl = getClplot(camval,thkval,angl) ;
                   plty[0][ic] = 100. * clpl ;
                   alfd = angl ;
                   thkd = thkinpt ;
                   camd = caminpt ;
//   attempt to fix symmetry problem
     if (camd < 0.0) alfd = - angl ;
//
                   solve.getDrag(clpl) ;
                   cdpl = dragco ;
                   pltx[0][ic] = 100.*cdpl ;
              }
              ntiky = 5 ;
              pltx[1][0] = cdref * 100. ;    
              plty[1][0] = clref * 100. ;                        
              labx = String.valueOf("Cd");
              labxu = String.valueOf("x 100");
              laby = String.valueOf("Cl");
              labyu = String.valueOf("x 100 ");
           }              

          if (dispp>= 2 && dispp < 6) {  // determine y - range zero in middle
              if(dout <=1) {
                 if (plty[0][npt] >= plty[0][1]) {
                     begy=0.0 ;
                     if (plty[0][1]   > endy) endy = plty[0][1]  ;
                     if (plty[0][npt] > endy) endy = plty[0][npt]  ;
                     if (endy <= 0.0) {
                        begy = plty[0][1] ;
                        endy = plty[0][npt] ;
                     }
                 }
                 if (plty[0][npt] < plty[0][1]) {
                     endy = 0.0 ;
                     if (plty[0][1]   < begy) begy = plty[0][1]  ;
                     if (plty[0][npt] < begy) begy = plty[0][npt]  ;
                     if (begy <= 0.0) {
                        begy = plty[0][npt] ;
                        endy = plty[0][1] ;
                     }
                 }
              }
              else {
                  begy = 0.0 ;
                  endy = 0.0 ;
                  for (index =1; index <= npt; ++ index) {
                      if (plty[0][index] > endy) endy = plty[0][index] ;
                  }
              }
          }

          if (dispp >= 6 && dispp <= 8) {    // determine y - range
              if (plty[0][npt] >= plty[0][1]) {
                 begy = plty[0][1]  ;
                 endy = plty[0][npt]  ;
              }
              if (plty[0][npt] < plty[0][1]) {
                 begy = plty[0][npt]  ;
                 endy = plty[0][1]  ;
              }
          }

          if (dispp == 9) {    // determine y - range and x- range
              begx = 0.0 ;
              endx = 0.0 ;
              for (index =1; index <= npt; ++ index) {
                  if (pltx[0][index] > endx) endx = pltx[0][index] ;
              }

              begy = plty[0][1]  ;
              endy = plty[0][1] ;
              for (index =1; index <= npt; ++ index) {
                  if (plty[0][index] > endy) endy = plty[0][index] ;
              }
          }

          if (dispp >= 0 && dispp <= 1) {    // determine y - range
              if (calcrange == 0) {
                 begy = plty[0][1] ;
                 endy = plty[0][1] ;
                 for (index = 1; index <= npt2; ++ index) {
                     if (plty[0][index] < begy) begy = plty[0][index] ;
                     if (plty[1][index] < begy) begy = plty[1][index] ;
                     if (plty[0][index] > endy) endy = plty[0][index] ;
                     if (plty[1][index] > endy) endy = plty[1][index] ;
                 }
                 calcrange = 1 ;
             }
          }
        }

        public double getClplot (double camb, double thic, double angl) {
           double beta,xc,yc,rc,gamc,lec,tec,lecm,tecm,crdc ;
           double stfact,number ;
    
           xc = 0.0 ;
           yc = camb / 2.0 ;
           rc = thic/4.0 + Math.sqrt( thic*thic/16.0 + yc*yc + 1.0);
           xc = 1.0 - Math.sqrt(rc*rc - yc*yc) ;
           beta = Math.asin(yc/rc)/convdr ;       /* Kutta condition */
           gamc = 2.0*rc*Math.sin((angl+beta)*convdr) ;
           if (foil <= 3 && anflag == 2) gamc = 0.0 ;

           lec = xc - Math.sqrt(rc*rc - yc*yc) ;
           tec = xc + Math.sqrt(rc*rc - yc*yc) ;
           lecm = lec + 1.0/lec ;
           tecm = tec + 1.0/tec ;
           crdc = tecm - lecm ;
                                      // stall model 1
           stfact = 1.0 ;
           if (anflag == 1) {
               if (angl > 10.0 ) {
                  stfact = .5 + .1 * angl - .005 * angl * angl ;
               }
               if (angl < -10.0 ) {
                  stfact = .5 - .1 * angl - .005 * angl * angl ;
               }
           }
    
           number = stfact*gamc*4.0*3.1415926/crdc ;

           if (arcor == 1) {  // correction for low aspect ratio
               number = number /(1.0 + Math.abs(number)/(3.14159*aspr)) ;
           }

           return (number) ;
        }
   
        public void update(Graphics g) {
           out.plt.paint(g) ;
        }
   
        public void paint(Graphics g) {
           int i,j,k,n,index ;
           int xlabel,ylabel,ind,inmax,inmin ;
           int exes[] = new int[8] ;
           int whys[] = new int[8] ;
           double offx,scalex,offy,scaley,waste,incy,incx;
           double xl,yl;
           double liftab,dragab ;
           int camx[] = new int[19] ;
           int camy[] = new int[19] ;
           Color col ;
  
           if (dispp <= 1) {
              off2Gg.setColor(Color.black) ;
              off2Gg.fillRect(0,0,350,350) ;
              off2Gg.setColor(Color.white) ;
              off2Gg.fillRect(2,185,70,20) ;
              off2Gg.setColor(Color.red) ;
              off2Gg.drawString("Rescale",8,200) ;
           }
           if (dispp > 1 && dispp <= 15) {
              off2Gg.setColor(Color.blue) ;
              off2Gg.fillRect(0,0,350,350) ;
              off2Gg.setColor(Color.white) ;
              off2Gg.fillRect(2,185,70,20) ;
              off2Gg.setColor(Color.red) ;
              off2Gg.drawString("Rescale",8,200) ;
           }
           if (dispp >= 20) {
              off2Gg.setColor(Color.black) ;
              off2Gg.fillRect(0,0,350,350) ;
              off2Gg.setColor(Color.white) ;
              off2Gg.drawString("Output",10,10) ;
           }
 
           if (ntikx < 2) ntikx = 2 ;     /* protection 13June96 */
           if (ntiky < 2) ntiky = 2 ;
           offx = 0.0 - begx ;
           scalex = 6.0/(endx-begx) ;
           incx = (endx-begx)/(ntikx-1);
           offy = 0.0 - begy ;
           scaley = 4.5/(endy-begy) ;
           incy = (endy-begy)/(ntiky-1) ;
 
           if (dispp <= 15) {             /*  draw a graph */
                                              /* draw axes */
              off2Gg.setColor(Color.white) ;
              exes[0] = (int) (factp* 0.0) + xtp ;
              whys[0] = (int) (factp* -4.5) + ytp ;
              exes[1] = (int) (factp* 0.0) + xtp ;
              whys[1] = (int) (factp* 0.0) + ytp ;
              exes[2] = (int) (factp* 6.0) + xtp ;
              whys[2] = (int) (factp* 0.0) + ytp ;
              off2Gg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
              off2Gg.drawLine(exes[1],whys[1],exes[2],whys[2]) ;
 
              xlabel = (int) (-90.0) + xtp ;   /*  label y axis */
              ylabel = (int) (factp*-1.5) + ytp ;
              off2Gg.drawString(laby,xlabel,ylabel) ;
              off2Gg.drawString(labyu,xlabel,ylabel+10) ;
                                                    /* add tick values */
              for (ind= 1; ind<= ntiky; ++ind){
                   xlabel = (int) (-50.0) + xtp ;
                   yl = begy + (ind-1) * incy ;
                   ylabel = (int) (factp* -scaley*(yl + offy)) + ytp ;
                   if (nord >= 2) {
                      if (endy >= 100.0) {
                            off2Gg.drawString(String.valueOf((int) yl),xlabel,ylabel) ;
                      }
                      if (endy <= 100.0 && endy >= 1.0) {
                            off2Gg.drawString(String.valueOf(filter1(yl)),xlabel,ylabel) ;
                      }
                      if (endy <= 1.0) {
                            off2Gg.drawString(String.valueOf(filter3(yl)),xlabel,ylabel) ;
                      }
                   }
                   else {
                      off2Gg.drawString(String.valueOf(filter3(yl)),xlabel,ylabel);
                   }
              }
              xlabel = (int) (factp*3.0) + xtp ;    /* label x axis */
              ylabel = (int) (40.0) + ytp ;
              off2Gg.drawString(labx,xlabel,ylabel-10) ;
              off2Gg.drawString(labxu,xlabel,ylabel) ;
                                                   /* add tick values */
              for (ind= 1; ind<= ntikx; ++ind){
                   ylabel = (int) (15.) + ytp ;
                   xl = begx + (ind-1) * incx ;
                   xlabel = (int) (factp*(scalex*(xl + offx) -.05)) + xtp ;
                   if (nabs == 1) {
                      off2Gg.drawString(String.valueOf(xl),xlabel,ylabel) ;
                   }
                   if (nabs > 1) {
                      off2Gg.drawString(String.valueOf((int) xl),xlabel,ylabel) ;
                   }
              }
       
              if(lines == 0) {
                 for (i=1; i<=npt; ++i) {
                     xlabel = (int) (factp*scalex*(offx+pltx[0][i])) + xtp ;
                     ylabel = (int)(factp*-scaley*(offy+plty[0][i]) +7.)+ytp;
                     off2Gg.drawString("*",xlabel,ylabel) ;
                 }
              }
              else {
                if (dispp <= 1) {
                 if (anflag != 1 || (anflag == 1 && Math.abs(alfval) < 10.0)) {

                   for (j=0; j<=ntr-1; ++j) {
                      k = 2 -j ;
                      if (k == 0) {
                        off2Gg.setColor(Color.magenta) ;
                        xlabel = (int) (factp* 6.1) + xtp ;
                        ylabel = (int) (factp* -2.5) + ytp ;
                        off2Gg.drawString("Upper",xlabel,ylabel) ;
                      }
                      if (k == 1) {
                        off2Gg.setColor(Color.yellow) ;
                        xlabel = (int) (factp* 6.1) + xtp ;
                        ylabel = (int) (factp* -1.5) + ytp ;
                        off2Gg.drawString("Lower",xlabel,ylabel) ;
                      }
                      if (k == 2) {
                        off2Gg.setColor(Color.green) ;
                        xlabel = (int) (factp* 2.0) + xtp ;
                        ylabel = (int) (factp* -5.0) + ytp ;
                        off2Gg.drawString("Free Stream",xlabel,ylabel) ;
                      }
                      exes[1] = (int) (factp*scalex*(offx+pltx[k][1])) + xtp;
                      whys[1] = (int) (factp*-scaley*(offy+plty[k][1]))+ ytp;
                      for (i=1; i<=npt; ++i) {
                        exes[0] = exes[1] ;
                        whys[0] = whys[1] ;
                        exes[1] = (int)(factp*scalex*(offx+pltx[k][i]))+xtp;
                        whys[1] = (int)(factp*-scaley*(offy+plty[k][i]))+ytp;
                        off2Gg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
                      }
                   }
                 }
                 if (anflag == 1 && Math.abs(alfval) > 10.0) {
                     off2Gg.setColor(Color.yellow) ;
                     xlabel = (int) (factp* 1.0) + xtp ;
                     ylabel = (int) (factp* -2.0) + ytp ;
                     off2Gg.drawString("Wing is Stalled",xlabel,ylabel) ;
       
                     xlabel = (int) (factp* 1.0) + xtp ;
                     ylabel = (int) (factp* -1.0) + ytp ;
                     off2Gg.drawString("Plot not Available",xlabel,ylabel) ;
                 }

               }
               if (dispp > 1) {
                 off2Gg.setColor(Color.white) ;
                 exes[1] = (int) (factp*scalex*(offx+pltx[0][1])) + xtp;
                 whys[1] = (int) (factp*-scaley*(offy+plty[0][1])) + ytp;
                 for (i=1; i<=npt; ++i) {
                   exes[0] = exes[1] ;
                   whys[0] = whys[1] ;
                   exes[1] = (int) (factp*scalex*(offx+pltx[0][i])) + xtp;
                   whys[1] = (int) (factp*-scaley*(offy+plty[0][i])) + ytp;
                   off2Gg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
                 }
                 xlabel = (int) (factp*scalex*(offx+pltx[1][0])) + xtp ;
                 ylabel = (int)(factp*-scaley*(offy+plty[1][0]))+ytp -4;
                 off2Gg.setColor(Color.red) ;
                 off2Gg.fillOval(xlabel,ylabel,5,5) ;
               }
             }
          }

          if(dispp == 20)  {      /*  draw the lift and drag gauge */
              off2Gg.setColor(Color.black) ;
              off2Gg.fillRect(0,100,300,30) ;
    // Thermometer Lift gage
              off2Gg.setColor(Color.white) ;
              if (lftout == 0) {
                 off2Gg.drawString("Lift =",70,75) ;
                 if (lunits == 0) off2Gg.drawString("Pounds",190,75) ;
                 if (lunits == 1) off2Gg.drawString("Newtons",190,75) ;
              }
              if (lftout == 1) off2Gg.drawString(" Cl  =",70,185) ;
    // Thermometer Drag gage
              if (dragOut == 0) {
                 off2Gg.drawString("Drag =",70,185) ;
                 if (lunits == 0) off2Gg.drawString("Pounds",190,185) ;
                 if (lunits == 1) off2Gg.drawString("Newtons",190,185) ;
              }
              if (dragOut == 1) off2Gg.drawString(" Cd  =",70,185) ;

              off2Gg.setColor(Color.yellow);
              for (index=0 ; index <= 10; index ++) {
                off2Gg.drawLine(7+index*25,100,7+index*25,110) ;
                off2Gg.drawString(String.valueOf(index),5+index*25,125) ;
                off2Gg.drawLine(7+index*25,130,7+index*25,140) ;
              }
 // Lift value
              liftab = lift ;
              if (lftout == 0) {
                 if (Math.abs(lift) <= 1.0) {
                    liftab = lift*10.0 ;
                    off2Gg.setColor(Color.cyan);
                    off2Gg.fillRect(0,100,7 + (int) (25*Math.abs(liftab)),10) ;
                    off2Gg.drawString("-1",180,70) ;
                 }
                 if (Math.abs(lift) > 1.0 && Math.abs(lift) <= 10.0) {
                    liftab = lift ;
                    off2Gg.setColor(Color.yellow);
                    off2Gg.fillRect(0,100,7 + (int) (25*Math.abs(liftab)),10) ;
                    off2Gg.drawString("0",180,70) ;
                 }
                 if (Math.abs(lift) > 10.0 && Math.abs(lift) <=100.0) {
                    liftab = lift/10.0 ;
                    off2Gg.setColor(Color.green);
                    off2Gg.fillRect(0,100,7 + (int) (25*Math.abs(liftab)),10) ;
                    off2Gg.drawString("1",180,70) ;
                 }
                 if (Math.abs(lift) > 100.0 && Math.abs(lift) <=1000.0) {
                    liftab = lift/100.0 ;
                    off2Gg.setColor(Color.red);
                    off2Gg.fillRect(0,100,7 + (int) (25*Math.abs(liftab)),10) ;
                    off2Gg.drawString("2",180,70) ;
                 }
                 if (Math.abs(lift) > 1000.0 && Math.abs(lift) <=10000.0) {
                    liftab = lift/1000.0 ;
                    off2Gg.setColor(Color.magenta);
                    off2Gg.fillRect(0,100,7 + (int) (25*Math.abs(liftab)),10) ;
                    off2Gg.drawString("3",180,70) ;
                 }
                 if (Math.abs(lift) > 10000.0 && Math.abs(lift) <=100000.0) {
                    liftab = lift/10000.0 ;
                    off2Gg.setColor(Color.orange);
                    off2Gg.fillRect(0,100,7 + (int) (25*Math.abs(liftab)),10) ;
                    off2Gg.drawString("4",180,70) ;
                 }
                 if (Math.abs(lift) > 100000.0 && Math.abs(lift) <=1000000.0) {
                    liftab = lift/100000.0 ;
                    off2Gg.setColor(Color.white);
                    off2Gg.fillRect(0,100,7 + (int) (25*Math.abs(liftab)),10) ;
                    off2Gg.drawString("5",180,70) ;
                 }
                 if (Math.abs(lift) > 1000000.0) {
                    liftab = lift/1000000.0 ;
                    off2Gg.setColor(Color.white);
                    off2Gg.fillRect(0,100,7 + (int) (25*Math.abs(liftab)),10) ;
                    off2Gg.drawString("6",180,70) ;
                 }
             }
          
             if (lftout == 1) {
                 liftab = clift ;
                 if (Math.abs(clift) <= 1.0) {
                    liftab = clift*10.0 ;
                    off2Gg.setColor(Color.cyan);
                    off2Gg.fillRect(0,100,7 + (int) (25*Math.abs(liftab)),10) ;
                    off2Gg.drawString("-1",180,70) ;
                 }
                 if (Math.abs(clift) > 1.0 && Math.abs(clift) <= 10.0) {
                    liftab = clift ;
                    off2Gg.setColor(Color.yellow);
                    off2Gg.fillRect(0,100,7 + (int) (25*Math.abs(liftab)),10) ;
                    off2Gg.drawString("0",180,70) ;
                 }
             }
 // Drag value
              dragab = drag ;
              if (dragOut == 0) {
                 if (Math.abs(drag) <= 1.0) {
                    dragab = drag*10.0 ;
                    off2Gg.setColor(Color.cyan);
                    off2Gg.fillRect(0,130,7 + (int) (25*Math.abs(dragab)),10) ;
                    off2Gg.drawString("-1",180,180) ;
                 }
                 if (Math.abs(drag) > 1.0 && Math.abs(drag) <= 10.0) {
                    dragab = drag ;
                    off2Gg.setColor(Color.yellow);
                    off2Gg.fillRect(0,130,7 + (int) (25*Math.abs(dragab)),10) ;
                    off2Gg.drawString("0",180,180) ;
                 }
                 if (Math.abs(drag) > 10.0 && Math.abs(drag) <=100.0) {
                    dragab = drag/10.0 ;
                    off2Gg.setColor(Color.green);
                    off2Gg.fillRect(0,130,7 + (int) (25*Math.abs(dragab)),10) ;
                    off2Gg.drawString("1",180,180) ;
                 }
                 if (Math.abs(drag) > 100.0 && Math.abs(drag) <=1000.0) {
                    dragab = drag/100.0 ;
                    off2Gg.setColor(Color.red);
                    off2Gg.fillRect(0,130,7 + (int) (25*Math.abs(dragab)),10) ;
                    off2Gg.drawString("2",180,180) ;
                 }
                 if (Math.abs(drag) > 1000.0 && Math.abs(drag) <=10000.0) {
                    dragab = drag/1000.0 ;
                    off2Gg.setColor(Color.magenta);
                    off2Gg.fillRect(0,130,7 + (int) (25*Math.abs(dragab)),10) ;
                    off2Gg.drawString("3",180,180) ;
                 }
                 if (Math.abs(drag) > 10000.0 && Math.abs(drag) <=100000.0) {
                    dragab = drag/10000.0 ;
                    off2Gg.setColor(Color.orange);
                    off2Gg.fillRect(0,130,7 + (int) (25*Math.abs(dragab)),10) ;
                    off2Gg.drawString("4",180,180) ;
                 }
                 if (Math.abs(drag) > 100000.0 && Math.abs(drag) <=1000000.0) {
                    dragab = drag/100000.0 ;
                    off2Gg.setColor(Color.white);
                    off2Gg.fillRect(0,130,7 + (int) (25*Math.abs(dragab)),10) ;
                    off2Gg.drawString("5",180,180) ;
                 }
                 if (Math.abs(drag) > 1000000.0) {
                    dragab = drag/1000000.0 ;
                    off2Gg.setColor(Color.white);
                    off2Gg.fillRect(0,130,7 + (int) (25*Math.abs(dragab)),10) ;
                    off2Gg.drawString("6",180,180) ;
                 }
             }
          
             if (dragOut == 1) {
                 dragab = dragCoeff ;
                 if (Math.abs(dragCoeff) <= .1) {
                    dragab = dragCoeff*100.0 ;
                    off2Gg.setColor(Color.magenta);
                    off2Gg.fillRect(0,130,7 + (int) (25*Math.abs(dragab)),10) ;
                    off2Gg.drawString("-2",180,180) ;
                 }
                 if (Math.abs(dragCoeff) > .1 && Math.abs(dragCoeff) <= 1.0) {
                    dragab = dragCoeff*10.0 ;
                    off2Gg.setColor(Color.cyan);
                    off2Gg.fillRect(0,130,7 + (int) (25*Math.abs(dragab)),10) ;
                    off2Gg.drawString("-1",180,180) ;
                 }
                 if (Math.abs(dragCoeff) > 1.0 && Math.abs(dragCoeff) <= 10.0) {
                    dragab = dragCoeff ;
                    off2Gg.setColor(Color.yellow);
                    off2Gg.fillRect(0,130,7 + (int) (25*Math.abs(dragab)),10) ;
                    off2Gg.drawString("0",180,180) ;
                 }
             }

             off2Gg.setColor(Color.white);
             off2Gg.drawString(String.valueOf(filter3(liftab)),110,75) ;
             off2Gg.drawString(" X 10 ",150,75) ;
             off2Gg.drawString(String.valueOf(filter3(dragab)),110,185) ;
             off2Gg.drawString(" X 10 ",150,185) ;
          }
          if(dispp == 25)  {      /*  draw the generating cylinder */
             off2Gg.setColor(Color.yellow) ;
             for (j=1; j<=nln2-1; ++j) {           /* lower half */
                for (i=1 ; i<= nptc-1; ++i) {
                   exes[0] = (int) (fact*xplg[j][i]) + xt ;
                   whys[0] = (int) (fact*(-yplg[j][i])) + yt ;
                   exes[1] = (int) (fact*xplg[j][i+1]) + xt ;
                   whys[1] = (int) (fact*(-yplg[j][i+1])) + yt ;
                   off2Gg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
                }
             }
                                                 // stagnation lines
             exes[1] = (int) (fact*xplg[nln2][1]) + xt ;
             whys[1] = (int) (fact*(-yplg[nln2][1])) + yt ;
             for (i=2 ; i<= npt2-1; ++i) {
                   exes[0] = exes[1] ;
                   whys[0] = whys[1] ;
                   exes[1] = (int) (fact*xplg[nln2][i]) + xt ;
                   whys[1] = (int) (fact*(-yplg[nln2][i])) + yt ;
                   off2Gg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
             }
             exes[1] = (int) (fact*xplg[nln2][npt2+1]) + xt ;
             whys[1] = (int) (fact*(-yplg[nln2][npt2+1])) + yt ;
             for (i=npt2+2 ; i<= nptc; ++i) {
                exes[0] = exes[1] ;
                whys[0] = whys[1] ;
                exes[1] = (int) (fact*xplg[nln2][i]) + xt ;
                whys[1] = (int) (fact*(-yplg[nln2][i])) + yt ;
                off2Gg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
             }
             for (j=nln2+1; j<=nlnc; ++j) {          /* upper half */
                for (i=1 ; i<= nptc-1; ++i) {
                   exes[0] = (int) (fact*xplg[j][i]) + xt ;
                   whys[0] = (int) (fact*(-yplg[j][i])) + yt ;
                   exes[1] = (int) (fact*xplg[j][i+1]) + xt ;
                   whys[1] = (int) (fact*(-yplg[j][i+1])) + yt ;
                   off2Gg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
                }
             }
                                         // draw the cylinder
             off2Gg.setColor(Color.white) ;
             exes[1] = (int) (fact*(xplg[0][npt2])) + xt ;
             whys[1] = (int) (fact*(-yplg[0][npt2])) + yt ;
             exes[2] = (int) (fact*(xplg[0][npt2])) + xt ;
             whys[2] = (int) (fact*(-yplg[0][npt2])) + yt ;
             for (i=1 ; i<= npt2-1; ++i) {
                exes[0] = exes[1] ;
                whys[0] = whys[1] ;
                exes[1] = (int) (fact*(xplg[0][npt2-i])) + xt ;
                whys[1] = (int) (fact*(-yplg[0][npt2-i])) + yt ;
                exes[3] = exes[2] ;
                whys[3] = whys[2] ;
                exes[2] = (int) (fact*(xplg[0][npt2+i])) + xt ;
                whys[2] = (int) (fact*(-yplg[0][npt2+i])) + yt ;
                off2Gg.fillPolygon(exes,whys,4) ;
             }
                                         // draw the axes
             off2Gg.setColor(Color.cyan) ;
             exes[1] = (int) (fact*(0.0)) + xt ;
             whys[1] = (int) (fact*(-10.0)) + yt ;
             exes[2] = (int) (fact*(0.0)) + xt ;
             whys[2] = (int) (fact*(10.0)) + yt ;
             off2Gg.drawLine(exes[1],whys[1],exes[2],whys[2]) ;
             exes[1] = (int) (fact*(-10.0)) + xt ;
             whys[1] = (int) (fact*(0.0)) + yt ;
             exes[2] = (int) (fact*(10.0)) + xt ;
             whys[2] = (int) (fact*(0.0)) + yt ;
             off2Gg.drawLine(exes[1],whys[1],exes[2],whys[2]) ;
                                         // draw the poles
             exes[1] = (int) (fact*(1.0)) + xt ;
             whys[1] = (int) (fact*(0.0)) + yt ;
             off2Gg.drawString("*",exes[1],whys[1]+5) ;
             exes[1] = (int) (fact*(-1.0)) + xt ;
             whys[1] = (int) (fact*(0.0)) + yt ;
             off2Gg.drawString("*",exes[1],whys[1]+5) ;
          }

          g.drawImage(offImg2,0,0,this) ;   
       }
     }     // Plt 

     class Prb extends Panel {
        Foil outerparent ;
        L l ;
        R r ;

        Prb (Foil target) {

           outerparent = target ;
           setLayout(new GridLayout(1,2,5,5)) ;

           l = new L(outerparent) ;
           r = new R(outerparent) ;

           add(l) ;
           add(r) ;
        }

        class L extends Panel {
           Foil outerparent ;
           Label l01 ;
           Button bt1,bt2,bt3 ;
     
           L (Foil target) {
            outerparent = target ;
            setLayout(new GridLayout(4,1,10,10)) ;

            l01 = new Label("Probe", Label.CENTER) ;
            l01.setForeground(Color.red) ;

            bt1 = new Button("Velocity") ;
            bt1.setBackground(Color.white) ;
            bt1.setForeground(Color.blue) ;

            bt2 = new Button("Pressure") ;
            bt2.setBackground(Color.white) ;
            bt2.setForeground(Color.blue) ;

            bt3 = new Button("Smoke") ;
            bt3.setBackground(Color.white) ;
            bt3.setForeground(Color.blue) ;
            add(l01) ;
            add(bt1) ;
            add(bt2) ;
            add(bt3) ;
          }

          public boolean action(Event evt, Object arg) {
            if(evt.target instanceof Button) {
               String label = (String)arg ;
               if(label.equals("Velocity")) {
                   pboflag = 1 ;
                   bt1.setBackground(Color.yellow) ;
                   bt2.setBackground(Color.white) ;
                   bt3.setBackground(Color.white) ;
               }
               if(label.equals("Pressure")) {
                   pboflag = 2 ;
                   bt2.setBackground(Color.yellow) ;
                   bt1.setBackground(Color.white) ;
                   bt3.setBackground(Color.white) ;
               }
               if(label.equals("Smoke")) {
                   pboflag = 3 ;
                   bt3.setBackground(Color.yellow) ;
                   bt2.setBackground(Color.white) ;
                   bt1.setBackground(Color.white) ;
               }

               computeFlow() ;
               return true ;
            }
            else return false ;
          } // Handler
        }  // Inl

        class R extends Panel {
            Foil outerparent ;
            Scrollbar s1,s2;
            L2 l2;
            Button bt4 ;

            R (Foil target) {

             outerparent = target ;
             setLayout(new BorderLayout(5,5)) ;

             s1 = new Scrollbar(Scrollbar.VERTICAL,550,10,0,1000);
             s2 = new Scrollbar(Scrollbar.HORIZONTAL,550,10,0,1000);

             l2 = new L2(outerparent) ;

             bt4 = new Button("OFF") ;
             bt4.setBackground(Color.red) ;
             bt4.setForeground(Color.white) ;

             add("West",s1) ;
             add("South",s2) ;
             add("Center",l2) ;
             add("North",bt4) ;
           }

           public boolean handleEvent(Event evt) {
                if(evt.id == Event.ACTION_EVENT) {
                   pboflag = 0 ;
                   l.bt3.setBackground(Color.white) ;
                   l.bt2.setBackground(Color.white) ;
                   l.bt1.setBackground(Color.white) ;
                   computeFlow() ;
                   return true ;
                }
                if(evt.id == Event.SCROLL_ABSOLUTE) {
                   this.handleBar(evt) ;
                   return true ;
                }
                if(evt.id == Event.SCROLL_LINE_DOWN) {
                   this.handleBar(evt) ;
                   return true ;
                }
                if(evt.id == Event.SCROLL_LINE_UP) {
                   this.handleBar(evt) ;
                   return true ;
                }
                if(evt.id == Event.SCROLL_PAGE_DOWN) {
                   this.handleBar(evt) ;
                   return true ;
                }
                if(evt.id == Event.SCROLL_PAGE_UP) {
                   this.handleBar(evt) ;
                   return true ;
                }
                else return false ;
           }

           public void handleBar(Event evt) {
             int i1,i2 ;

             i1 = s1.getValue() ;
             i2 = s2.getValue() ;

             ypval = 5.0 - i1 * 10.0/ 1000. ;
             xpval = i2 * 20.0/ 1000. -10.0 ;
    
             computeFlow() ;
           }

           class L2 extends Canvas  {
              Foil outerparent ;

              L2 (Foil target) {
                setBackground(Color.black) ;
              }

              public void update(Graphics g) {
                out.prb.r.l2.paint(g) ;
              }

              public void paint(Graphics g) {
                int ex,ey,index ;
                double pbout ;
    
                off3Gg.setColor(Color.black) ;
                off3Gg.fillRect(0,0,150,150) ;

                if (pboflag == 0 || pboflag == 3)off3Gg.setColor(Color.gray);
                if (pboflag == 1 || pboflag == 2)off3Gg.setColor(Color.yellow) ;
                off3Gg.fillArc(20,30,80,80,-23,227) ;
                off3Gg.setColor(Color.black) ;
         // tick marks
                for (index = 1; index <= 4; ++ index) {
                    ex = 60 + (int) (50.0 * Math.cos(convdr * (-22.5 + 45.0 * index))) ;
                    ey = 70 - (int) (50.0 * Math.sin(convdr * (-22.5 + 45.0 * index))) ;
                    off3Gg.drawLine(60,70,ex,ey) ;
                }
                off3Gg.fillArc(25,35,70,70,-25,235) ;
      
                off3Gg.setColor(Color.yellow) ;
                off3Gg.drawString("0",10,95) ;
                off3Gg.drawString("2",10,55) ;
                off3Gg.drawString("4",35,30) ;
                off3Gg.drawString("6",75,30) ;
                off3Gg.drawString("8",100,55) ;
                off3Gg.drawString("10",100,95) ;

                off3Gg.setColor(Color.green) ;
                if (pboflag == 1) {
                    off3Gg.drawString("Velocity",40,15) ;
                    if (lunits == 0) off3Gg.drawString("mph",50,125) ;
                    if (lunits == 1) off3Gg.drawString("km/h",50,125) ;
                }
                if (pboflag == 2) {
                    off3Gg.drawString("Pressure",30,15) ;
                    if (lunits == 0) off3Gg.drawString("psi",50,125) ;
                    if (lunits == 1) off3Gg.drawString("kPa",50,125) ;
                }

                off3Gg.setColor(Color.green) ;
                off3Gg.drawString("x 10",65,110) ;

                ex = 60 ;
                ey = 70 ;
               
                pbout = 0.0 ;
                if (pbval <= .001) {
                   pbout = pbval * 1000. ;
                   off3Gg.drawString("-4",90,105) ;
                }
                if (pbval <= .01 && pbval > .001) {
                   pbout = pbval * 100. ;
                   off3Gg.drawString("-3",90,105) ;
                }
                if (pbval <= .1 && pbval > .01) {
                   pbout = pbval * 10. ;
                   off3Gg.drawString("-2",90,105) ;
                }
                if (pbval <= 1 && pbval > .1) {
                   pbout = pbval * 10. ;
                   off3Gg.drawString("-1",90,105) ;
                }
                if (pbval <= 10 && pbval > 1) {
                   pbout = pbval  ;
                   off3Gg.drawString("0",90,105) ;
                }
                if (pbval <= 100 && pbval > 10) {
                   pbout = pbval * .1 ;
                   off3Gg.drawString("1",90,105) ;
                }
                if (pbval <= 1000 && pbval > 100) {
                   pbout = pbval * .01 ;
                   off3Gg.drawString("2",90,105) ;
                }
                if (pbval > 1000) {
                   pbout = pbval * .001 ;
                   off3Gg.drawString("3",90,105) ;
                }
                off3Gg.setColor(Color.green) ;
                off3Gg.drawString(String.valueOf(filter3(pbout)),30,110) ;

                off3Gg.setColor(Color.yellow) ;
                ex = 60 - (int) (30.0 * Math.cos(convdr *
                           (-22.5 + pbout * 225. /10.0))) ;
                ey = 70 - (int) (30.0 * Math.sin(convdr *
                           (-22.5 + pbout * 225. /10.0))) ;
                off3Gg.drawLine(60,70,ex,ey) ;

                g.drawImage(offImg3,0,0,this) ;
              }
           } //L2
        }  // Inr
     }  // Prb

     class Perf extends Panel {
        Foil outerparent ;
        TextArea prnt ;

        Perf (Foil target) {

           setLayout(new GridLayout(1,1,0,0)) ;

           prnt = new TextArea() ;
           prnt.setEditable(false) ;

           prnt.appendText("FoilSim III 1.4d beta - 21 Mar 11 ") ;
           add(prnt) ;
        }
     }  // Perf
  } // Out 

  class Viewer extends Canvas  
         implements Runnable{
     Foil outerparent ;
     Thread runner ;
     Point locate,anchor;

     Viewer (Foil target) {
         setBackground(Color.black) ;
         runner = null ;
     } 

     public Insets insets() {
        return new Insets(0,10,0,10) ;
     }
 
     public boolean mouseDown(Event evt, int x, int y) {
        anchor = new Point(x,y) ;
        return true;
     }

     public boolean mouseUp(Event evt, int x, int y) {
        handleb(x,y) ;
        return true;
     }

     public boolean mouseDrag(Event evt, int x, int y) {
        handle(x,y) ;
        return true;
     }

     public void handle(int x, int y) {
         // determine location
         if (y >= 30) { 
             if (x >= 30 ) {   // translate
                if (displ != 2) {
                  locate = new Point(x,y) ;
                  yt =  yt + (int) (.2*(locate.y - anchor.y)) ;
                  xt =  xt + (int) (.4*(locate.x - anchor.x))  ;
                  if (xt > 320) xt = 320 ;
                  if (xt < -280) xt = -280 ;
                  if (yt > 300) yt = 300 ;
                  if (yt <-300) yt = -300 ;
                  xt1 = xt + spanfac ;
                  yt1 = yt - spanfac ;
                  xt2 = xt - spanfac;
                  yt2 = yt + spanfac ;
                }
                if(displ == 2)  {          // move the rake
                  locate = new Point(x,y) ;
                  xflow = xflow + .01*(locate.x - anchor.x) ;
                  if (xflow < -10.0) xflow = -10.0 ;
                  if (xflow > 0.0) xflow = 0.0 ;
                  computeFlow() ;
                }
             }
             if (x < 30 ) {   // zoom widget
               sldloc = y ;
               if (sldloc < 30) sldloc = 30;
               if (sldloc > 165) sldloc = 165;
               fact = 10.0 + (sldloc-30)*1.0 ;
               spanfac = (int)(2.0*fact*aspr*.3535) ;
               xt1 = xt + spanfac ;
               yt1 = yt - spanfac ;
               xt2 = xt - spanfac;
               yt2 = yt + spanfac ;
             }
         }
     }

     public void handleb(int x, int y) {
         if (y < 15) { 
             if (x >= 90 && x <= 139) {   //edge view
                  viewflg = 0 ;
             }
             if (x >= 140 && x <= 170) {   //top view
                  if (foil <= 4) viewflg = 1 ;
                  if (foil == 5) viewflg = 0 ;
                  displ = 3 ;
                  pboflag = 0 ;
             }
             if (x >= 171 && x <= 239) {   //side view
                  if (foil <= 4) viewflg = 2 ;
                  if (foil == 5) viewflg = 0 ;
             }
             if (x >= 240 && x <= 270) {   //find
                  xt = 170;  yt = 105; fact = 30.0 ;
                  sldloc = 50 ;
                  spanfac = (int)(2.0*fact*aspr*.3535) ;
                  xt1 = xt + spanfac ;
                  yt1 = yt - spanfac ;
                  xt2 = xt - spanfac;
                  yt2 = yt + spanfac ;
             }
         }
         if (y > 15 && y <= 30) { 
             if (x >= 80 && x <= 154) {   //display streamlines
                 if(viewflg != 1)  displ = 0 ;
             }
             if (x >= 155 && x <= 204) {   //display animation
                 if(viewflg != 1) displ = 1 ;
             }
             if (x >= 205 && x <= 249) {   //display direction
                 if(viewflg != 1) displ = 2 ;
             }
             if (x >= 250 && x <= 330) {   //display geometry
                  displ = 3 ;
                  pboflag = 0 ;
             }
         }
         view.repaint() ;
     }

     public void start() {
        if (runner == null) {
           runner = new Thread(this) ;
           runner.start() ;
        }
        antim = 0 ;                              /* MODS  21 JUL 99 */
        ancol = 1 ;                              /* MODS  27 JUL 99 */
     }

     public void run() {
       int timer ;
 
       timer = 100 ;
       while (true) {
          ++ antim ;
          try { Thread.sleep(timer); }
          catch (InterruptedException e) {}
          view.repaint() ;
          if (antim == 3) {
             antim = 0;
             ancol = - ancol ;               /* MODS 27 JUL 99 */
          }
          timer = 135 - (int) (.227 *vfsd/vconv) ;
                                            // make the ball spin
          if (foil >= 4) {
             plthg[1] = plthg[1] + spin*spindr*5. ;
             if (plthg[1] < -360.0) {
                plthg[1] = plthg[1] + 360.0 ;
             }
             if (plthg[1] > 360.0) {
                plthg[1] = plthg[1] - 360.0 ;
             }
          }
       }
     }

     public void update(Graphics g) {
        view.paint(g) ;
     }
 
     public void paint(Graphics g) {
        int i,j,k,n ;
        int xlabel,ylabel,ind,inmax,inmin ;
        int exes[] = new int[8] ;
        int whys[] = new int[8] ;
        double offx,scalex,offy,scaley,waste,incy,incx;
        double xl,yl,slope,radvec,xvec,yvec ;
        int camx[] = new int[19] ;
        int camy[] = new int[19] ;
        Color col ;

        col = new Color(0,0,0) ;
        if(planet == 0) col = Color.cyan ;
        if(planet == 1) col = Color.orange ;
        if(planet == 2) col = Color.green ;
        if(planet >= 3) col = Color.cyan ;
        off1Gg.setColor(Color.black) ;
        off1Gg.fillRect(0,0,500,500) ;

        if (viewflg == 1) {              // Top View
          off1Gg.setColor(Color.white) ;
          exes[0] = (int) (.25*fact*(-span)) + xt ;
          whys[0] = (int) (.25*fact*(-chord)) + yt ;
          exes[1] = (int) (.25*fact*(-span)) + xt ;
          whys[1] = (int) (.25*fact*(chord)) + yt ;
          exes[2] = (int) (.25*fact*(span)) + xt ;
          whys[2] = (int) (.25*fact*(chord)) + yt ;
          exes[3] = (int) (.25*fact*(span)) + xt ;
          whys[3] = (int) (.25*fact*(-chord)) + yt ;
          off1Gg.fillPolygon(exes,whys,4) ;
          off1Gg.setColor(Color.green) ;
          off1Gg.drawLine(exes[0],whys[1]+5,exes[2],whys[1]+5) ;
          off1Gg.drawString("Span",exes[2]-20,whys[1]+20) ;
          off1Gg.drawLine(exes[2]+5,whys[0],exes[2]+5,whys[1]) ;
          if (foil <= 3) off1Gg.drawString("Chord",exes[2]+10,55) ;
          if (foil == 4) off1Gg.drawString("Diameter",exes[2]+10,55) ;

          off1Gg.setColor(Color.green) ;
          off1Gg.drawString("Flow",45,145) ;
          off1Gg.drawLine(40,155,40,125) ;
          exes[0] = 35 ;  exes[1] = 45; exes[2] = 40 ;
          whys[0] = 125 ;  whys[1] = 125; whys[2] = 115 ;
          off1Gg.fillPolygon(exes,whys,3) ;
        }

        if (viewflg == 0 || viewflg == 2) {  // edge View
         if (vfsd > .01) {
                                            /* plot airfoil flowfield */
          radvec = .5 ;
          for (j=1; j<=nln2-1; ++j) {           /* lower half */
             for (i=1 ; i<= nptc-1; ++i) {
                exes[0] = (int) (fact*xpl[j][i]) + xt ;
                whys[0] = (int) (fact*(-ypl[j][i])) + yt ;
                slope = (ypl[j][i+1]-ypl[j][i])/(xpl[j][i+1]-xpl[j][i]) ;
                xvec = xpl[j][i] + radvec / Math.sqrt(1.0 + slope*slope) ;
                yvec = ypl[j][i] + slope * (xvec - xpl[j][i]) ;
                exes[1] = (int) (fact*xvec) + xt ;
                whys[1] = (int) (fact*(-yvec)) + yt ;
                if (displ == 0) {                   /* MODS  21 JUL 99 */
                  off1Gg.setColor(Color.yellow) ;
                  exes[1] = (int) (fact*xpl[j][i+1]) + xt ;
                  whys[1] = (int) (fact*(-ypl[j][i+1])) + yt ;
                  off1Gg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
                }
                if (displ == 2 && (i/3*3 == i) ) {
                  off1Gg.setColor(col) ;
                  for (n=1 ; n <= 4 ; ++n) {
                     if(i == 6 + (n-1)*9) off1Gg.setColor(Color.yellow);
                  }
                  if(i/9*9 == i) off1Gg.setColor(Color.white);
                  off1Gg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
                }
                if (displ == 1 && ((i-antim)/3*3 == (i-antim)) ) {
                  if (ancol == -1) {          /* MODS  27 JUL 99 */
                    if((i-antim)/6*6 == (i-antim))off1Gg.setColor(col);
                    if((i-antim)/6*6 != (i-antim))off1Gg.setColor(Color.white);
                  }
                  if (ancol == 1) {          /* MODS  27 JUL 99 */
                    if((i-antim)/6*6 == (i-antim))off1Gg.setColor(Color.white);
                    if((i-antim)/6*6 != (i-antim))off1Gg.setColor(col);
                  }
                  off1Gg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
                }
             }
          }
 
          off1Gg.setColor(Color.white) ; /* stagnation */
          exes[1] = (int) (fact*xpl[nln2][1]) + xt ;
          whys[1] = (int) (fact*(-ypl[nln2][1])) + yt ;
          for (i=2 ; i<= npt2-1; ++i) {
                exes[0] = exes[1] ;
                whys[0] = whys[1] ;
                exes[1] = (int) (fact*xpl[nln2][i]) + xt ;
                whys[1] = (int) (fact*(-ypl[nln2][i])) + yt ;
                if (displ <= 2) {             /* MODS  21 JUL 99 */
                  off1Gg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
                }
          }
          exes[1] = (int) (fact*xpl[nln2][npt2+1]) + xt ;
          whys[1] = (int) (fact*(-ypl[nln2][npt2+1])) + yt ;
          for (i=npt2+2 ; i<= nptc; ++i) {
                exes[0] = exes[1] ;
                whys[0] = whys[1] ;
                exes[1] = (int) (fact*xpl[nln2][i]) + xt ;
                whys[1] = (int) (fact*(-ypl[nln2][i])) + yt ;
                if (displ <= 2) {                         /* MODS  21 JUL 99 */
                  off1Gg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
                }
          }
                                               /*  probe location */
          if (pboflag > 0 && pypl <= 0.0) {
             off1Gg.setColor(Color.magenta) ;
             off1Gg.fillOval((int) (fact*pxpl) + xt,
                  (int) (fact*(-pypl)) + yt - 2,5,5);
             off1Gg.setColor(Color.white) ;
             exes[0] = (int) (fact*(pxpl + .1)) +xt ;
             whys[0] = (int) (fact*(-pypl)) + yt ;
             exes[1] = (int) (fact*(pxpl + .5)) +xt ;
             whys[1] = (int) (fact*(-pypl)) + yt ;
             exes[2] = (int) (fact*(pxpl + .5)) +xt ;
             whys[2] = (int) (fact*(-pypl +50.)) +yt ;
             off1Gg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
             off1Gg.drawLine(exes[1],whys[1],exes[2],whys[2]) ;
             if (pboflag == 3) {    /* smoke trail  MODS  21 JUL 99 */
               off1Gg.setColor(Color.green) ;
               for (i=1 ; i<= nptc-1; ++i) {
                  exes[0] = (int) (fact*xpl[19][i]) + xt ;
                  whys[0] = (int) (fact*(-ypl[19][i])) + yt ;
                  slope = (ypl[19][i+1]-ypl[19][i])/(xpl[19][i+1]-xpl[19][i]) ;
                  xvec = xpl[19][i] + radvec / Math.sqrt(1.0 + slope*slope) ;
                  yvec = ypl[19][i] + slope * (xvec - xpl[19][i]) ;
                  exes[1] = (int) (fact*xvec) + xt ;
                  whys[1] = (int) (fact*(-yvec)) + yt ;
                  if ((i-antim)/3*3 == (i-antim) ) {
                    off1Gg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
                  }
               }
             }
          }
 
 //  wing surface
          if (viewflg == 2) {           // 3d geom
             off1Gg.setColor(Color.red) ;
             exes[1] = (int) (fact*(xpl[0][npt2])) + xt1 ;
             whys[1] = (int) (fact*(-ypl[0][npt2])) + yt1 ;
             exes[2] = (int) (fact*(xpl[0][npt2])) + xt2 ;
             whys[2] = (int) (fact*(-ypl[0][npt2])) + yt2 ;
             for (i=1 ; i<= npt2-1; ++i) {
                exes[0] = exes[1] ;
                whys[0] = whys[1] ;
                exes[1] = (int) (fact*(xpl[0][npt2-i])) + xt1 ;
                whys[1] = (int) (fact*(-ypl[0][npt2-i])) + yt1 ;
                exes[3] = exes[2] ;
                whys[3] = whys[2] ;
                exes[2] = (int) (fact*(xpl[0][npt2-i])) + xt2 ;
                whys[2] = (int) (fact*(-ypl[0][npt2-i])) + yt2 ;
                off1Gg.fillPolygon(exes,whys,4) ;
             }
          }

          for (j=nln2+1; j<=nlnc; ++j) {          /* upper half */
             for (i=1 ; i<= nptc-1; ++i) {
                exes[0] = (int) (fact*xpl[j][i]) + xt ;
                whys[0] = (int) (fact*(-ypl[j][i])) + yt ;
                slope = (ypl[j][i+1]-ypl[j][i])/(xpl[j][i+1]-xpl[j][i]) ;
                xvec = xpl[j][i] + radvec / Math.sqrt(1.0 + slope*slope) ;
                yvec = ypl[j][i] + slope * (xvec - xpl[j][i]) ;
                exes[1] = (int) (fact*xvec) + xt ;
                whys[1] = (int) (fact*(-yvec)) + yt ;
                if (displ == 0) {                     /* MODS  21 JUL 99 */
                  off1Gg.setColor(col) ;
                  exes[1] = (int) (fact*xpl[j][i+1]) + xt ;
                  whys[1] = (int) (fact*(-ypl[j][i+1])) + yt ;
                  off1Gg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
                }
                if (displ == 2 && (i/3*3 == i) ) {
                  off1Gg.setColor(col);   /* MODS  27 JUL 99 */
                  for (n=1 ; n <= 4 ; ++n) {
                     if(i == 6 + (n-1)*9) off1Gg.setColor(Color.yellow);
                  }
                  if(i/9*9 == i) off1Gg.setColor(Color.white);
                  off1Gg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
                }
                if (displ == 1 && ((i-antim)/3*3 == (i-antim)) ) {
                  if (ancol == -1) {          /* MODS  27 JUL 99 */
                    if((i-antim)/6*6 == (i-antim))off1Gg.setColor(col);
                    if((i-antim)/6*6 != (i-antim))off1Gg.setColor(Color.white);
                  }
                  if (ancol == 1) {          /* MODS  27 JUL 99 */
                    if((i-antim)/6*6 == (i-antim))off1Gg.setColor(Color.white);
                    if((i-antim)/6*6 != (i-antim))off1Gg.setColor(col);
                  }
                  off1Gg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
                }
             }
          }
                                               /*  probe location */
          if (pboflag > 0 && pypl > 0.0) {
             off1Gg.setColor(Color.magenta) ;
             off1Gg.fillOval((int) (fact*pxpl) + xt,
                  (int) (fact*(-pypl)) + yt - 2,5,5);
             off1Gg.setColor(Color.white) ;
             exes[0] = (int) (fact*(pxpl + .1)) +xt ;
             whys[0] = (int) (fact*(-pypl)) + yt ;
             exes[1] = (int) (fact*(pxpl + .5)) +xt ;
             whys[1] = (int) (fact*(-pypl)) + yt ;
             exes[2] = (int) (fact*(pxpl + .5)) +xt ;
             whys[2] = (int) (fact*(-pypl -50.)) +yt ;
             off1Gg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
             off1Gg.drawLine(exes[1],whys[1],exes[2],whys[2]) ;
             if (pboflag == 3) {    /* smoke trail  MODS  21 JUL 99 */
               off1Gg.setColor(Color.green) ;
               for (i=1 ; i<= nptc-1; ++i) {
                  exes[0] = (int) (fact*xpl[19][i]) + xt ;
                  whys[0] = (int) (fact*(-ypl[19][i])) + yt ;
                  slope = (ypl[19][i+1]-ypl[19][i])/(xpl[19][i+1]-xpl[19][i]) ;
                  xvec = xpl[19][i] + radvec / Math.sqrt(1.0 + slope*slope) ;
                  yvec = ypl[19][i] + slope * (xvec - xpl[19][i]) ;
                  exes[1] = (int) (fact*xvec) + xt ;
                  whys[1] = (int) (fact*(-yvec)) + yt ;
                  if ((i-antim)/3*3 == (i-antim) ) {
                    off1Gg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
                  }
               }
             }
           }
         }
 
         if (viewflg == 0) {
  // draw the airfoil geometry
             off1Gg.setColor(Color.white) ;
             exes[1] = (int) (fact*(xpl[0][npt2])) + xt ;
             whys[1] = (int) (fact*(-ypl[0][npt2])) + yt ;
             exes[2] = (int) (fact*(xpl[0][npt2])) + xt ;
             whys[2] = (int) (fact*(-ypl[0][npt2])) + yt ;
             for (i=1 ; i<= npt2-1; ++i) {
                exes[0] = exes[1] ;
                whys[0] = whys[1] ;
                exes[1] = (int) (fact*(xpl[0][npt2-i])) + xt ;
                whys[1] = (int) (fact*(-ypl[0][npt2-i])) + yt ;
                exes[3] = exes[2] ;
                whys[3] = whys[2] ;
                exes[2] = (int) (fact*(xpl[0][npt2+i])) + xt ;
                whys[2] = (int) (fact*(-ypl[0][npt2+i])) + yt ;
                camx[i] = (exes[1] + exes[2]) / 2 ;
                camy[i] = (whys[1] + whys[2]) / 2 ;
                if (foil == 3) {
                    off1Gg.setColor(Color.yellow) ;
                    off1Gg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
                }
                else {
                    off1Gg.setColor(Color.white) ;
                    off1Gg.fillPolygon(exes,whys,4) ;
                }
             }
   // put some info on the geometry
             if (displ == 3) {
                if (foil <= 3) {
                   inmax = 1 ;
                   for (n=1; n <= nptc; ++n) {
                     if(xpl[0][n] > xpl[0][inmax]) inmax = n ;
                   }
                   off1Gg.setColor(Color.green) ;
                   exes[0] = (int) (fact*(xpl[0][inmax])) + xt ;
                   whys[0] = (int) (fact*(-ypl[0][inmax])) + yt ;
                   off1Gg.drawLine(exes[0],whys[0],exes[0]-250,whys[0]) ;
                   off1Gg.drawString("Reference",30,whys[0]+10) ;
                   off1Gg.drawString("Angle",exes[0]+20,whys[0]) ;
      
                   off1Gg.setColor(Color.cyan) ;
                   exes[1] = (int) (fact*(xpl[0][inmax] -
                         4.0*Math.cos(convdr*alfval)))+xt;
                   whys[1] = (int) (fact*(-ypl[0][inmax] -
                         4.0*Math.sin(convdr*alfval)))+yt;
                   off1Gg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
                   off1Gg.drawString("Chord Line",exes[0]+20,whys[0]+20) ;
   
                   off1Gg.setColor(Color.red) ;
                   off1Gg.drawLine(exes[1],whys[1],camx[5],camy[5]) ;
                   for (i=7 ; i<= npt2-6; i = i+2) {
                      off1Gg.drawLine(camx[i],camy[i],camx[i+1],camy[i+1]) ;
                   }
                   off1Gg.drawString("Mean Camber Line",exes[0]-70,whys[1]-10) ;
                }
                if (foil >= 4) {
                   off1Gg.setColor(Color.red) ;
                   exes[0] = (int) (fact*(xpl[0][1])) + xt ;
                   whys[0] = (int) (fact*(-ypl[0][1])) + yt ;
                   exes[1] = (int) (fact*(xpl[0][npt2])) +xt ;
                   whys[1] = (int) (fact*(-ypl[0][npt2])) + yt ;
                   off1Gg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
                   off1Gg.drawString("Diameter",exes[0]+20,whys[0]+20) ;
                }
   
                off1Gg.setColor(Color.green) ;
                off1Gg.drawString("Flow",30,145) ;
                off1Gg.drawLine(30,152,60,152) ;
                exes[0] = 60 ;  exes[1] = 60; exes[2] = 70 ;
                whys[0] = 157 ;  whys[1] = 147 ; whys[2] = 152  ;
                off1Gg.fillPolygon(exes,whys,3) ;
             }
                                     //  spin the cylinder and ball
             if (foil >= 4) {
                exes[0] = (int) (fact* (.5*(xpl[0][1] + xpl[0][npt2]) +
                     rval * Math.cos(convdr*(plthg[1] + 180.)))) + xt ;
                whys[0] = (int) (fact* (-ypl[0][1] +
                     rval * Math.sin(convdr*(plthg[1] + 180.)))) + yt ;
                exes[1] = (int) (fact* (.5*(xpl[0][1] + xpl[0][npt2]) +
                     rval * Math.cos(convdr*plthg[1]))) + xt ;
                whys[1] = (int) (fact* (-ypl[0][1] +
                     rval * Math.sin(convdr*plthg[1]))) + yt ;
                off1Gg.setColor(Color.red) ;
                off1Gg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
             }
          }
          if (viewflg == 2) {
 //   front foil
             off1Gg.setColor(Color.white) ;
             exes[1] = (int) (fact*(xpl[0][npt2])) + xt2 ;
             whys[1] = (int) (fact*(-ypl[0][npt2])) + yt2 ;
             exes[2] = (int) (fact*(xpl[0][npt2])) + xt2 ;
             whys[2] = (int) (fact*(-ypl[0][npt2])) + yt2 ;
             for (i=1 ; i<= npt2-1; ++i) {
                exes[0] = exes[1] ;
                whys[0] = whys[1] ;
                exes[1] = (int) (fact*(xpl[0][npt2-i])) + xt2 ;
                whys[1] = (int) (fact*(-ypl[0][npt2-i])) + yt2 ;
                exes[3] = exes[2] ;
                whys[3] = whys[2] ;
                exes[2] = (int) (fact*(xpl[0][npt2+i])) + xt2 ;
                whys[2] = (int) (fact*(-ypl[0][npt2+i])) + yt2 ;
                camx[i] = (exes[1] + exes[2]) / 2 ;
                camy[i] = (whys[1] + whys[2]) / 2 ;
                off1Gg.fillPolygon(exes,whys,4) ;
             }
  // put some info on the geometry
             if (displ == 3) {
                off1Gg.setColor(Color.green) ;
                exes[1] = (int) (fact*(xpl[0][1])) + xt1 + 20 ;
                whys[1] = (int) (fact*(-ypl[0][1])) + yt1 ;
                exes[2] = (int) (fact*(xpl[0][1])) + xt2 + 20 ;
                whys[2] = (int) (fact*(-ypl[0][1])) + yt2 ;
                off1Gg.drawLine(exes[1],whys[1],exes[2],whys[2]) ;
                off1Gg.drawString("Span",exes[2]+10,whys[2]+10) ;

                exes[1] = (int) (fact*(xpl[0][1])) + xt2 ;
                whys[1] = (int) (fact*(-ypl[0][1])) + yt2 + 15 ;
                exes[2] = (int) (fact*(xpl[0][npt2])) + xt2  ;
                whys[2] = whys[1] ;
                off1Gg.drawLine(exes[1],whys[1],exes[2],whys[2]) ;
                if (foil <= 3) off1Gg.drawString("Chord",exes[2]+10,whys[2]+15);
                if (foil >= 4) off1Gg.drawString("Diameter",exes[2]+10,whys[2]+15);

                off1Gg.drawString("Flow",40,75) ;
                off1Gg.drawLine(30,82,60,82) ;
                exes[0] = 60 ;  exes[1] = 60; exes[2] = 70 ;
                whys[0] = 87 ;  whys[1] = 77 ; whys[2] = 82  ;
                off1Gg.fillPolygon(exes,whys,3) ;
             }
                                     //  spin the cylinder and ball
             if (foil >= 4) {
                exes[0] = (int) (fact* (.5*(xpl[0][1] + xpl[0][npt2]) +
                     rval * Math.cos(convdr*(plthg[1] + 180.)))) + xt2 ;
                whys[0] = (int) (fact* (-ypl[0][1] +
                     rval * Math.sin(convdr*(plthg[1] + 180.)))) + yt2 ;
                exes[1] = (int) (fact* (.5*(xpl[0][1] + xpl[0][npt2]) +
                     rval * Math.cos(convdr*plthg[1]))) + xt2 ;
                whys[1] = (int) (fact* (-ypl[0][1] +
                     rval * Math.sin(convdr*plthg[1]))) + yt2 ;
                off1Gg.setColor(Color.red) ;
                off1Gg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
             }
          }
        }

// Labels
        off1Gg.setColor(Color.black) ;
        off1Gg.fillRect(0,0,350,30) ;
        off1Gg.setColor(Color.white) ;
        off1Gg.drawString("View:",35,10) ;
        if (viewflg == 0) off1Gg.setColor(Color.yellow) ;
        else off1Gg.setColor(Color.cyan) ;
        off1Gg.drawString("Edge",95,10) ;
        if (viewflg == 1) off1Gg.setColor(Color.yellow) ;
        else off1Gg.setColor(Color.cyan) ;
        off1Gg.drawString("Top",145,10) ;
        if (viewflg == 2) off1Gg.setColor(Color.yellow) ;
        else off1Gg.setColor(Color.cyan) ;
        off1Gg.drawString("Side-3D",180,10) ;
        off1Gg.setColor(Color.red) ;
        off1Gg.drawString("Find",240,10) ;

        if (displ == 0) off1Gg.setColor(Color.yellow) ;
        else off1Gg.setColor(Color.cyan) ;
        off1Gg.drawString("Streamlines",85,25) ;
        if (displ == 1) off1Gg.setColor(Color.yellow) ;
        else off1Gg.setColor(Color.cyan) ;
        off1Gg.drawString("Moving",160,25) ;
        if (displ == 2) off1Gg.setColor(Color.yellow) ;
        else off1Gg.setColor(Color.cyan) ;
        off1Gg.drawString("Frozen",210,25) ;
        if (displ == 3) off1Gg.setColor(Color.yellow) ;
        else off1Gg.setColor(Color.cyan) ;
        off1Gg.drawString("Geometry",260,25) ;
        off1Gg.setColor(Color.white) ;
        off1Gg.drawString("Display:",35,25) ;
 // zoom in
        off1Gg.setColor(Color.black) ;
        off1Gg.fillRect(0,30,30,150) ;
        off1Gg.setColor(Color.green) ;
        off1Gg.drawString("Zoom",2,180) ;
        off1Gg.drawLine(15,35,15,165) ;
        off1Gg.fillRect(5,sldloc,20,5) ;

        g.drawImage(offImg1,0,0,this) ;   
    }
  } // end Viewer
}
