package net.wigle.wigleandroid;

import java.util.List;

import net.wigle.wigleandroid.MainActivity.Doer;
import android.app.Activity;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * configure settings
 */
public final class DataActivity extends Activity {
  
  private static final int MENU_EXIT = 11;
  private static final int MENU_SETTINGS = 12;
  private static final int MENU_ERROR_REPORT = 13;
  
  /** Called when the activity is first created. */
  @Override
  public void onCreate( final Bundle savedInstanceState) {
      super.onCreate( savedInstanceState );
      setContentView( R.layout.data );
      
      // force media volume controls
      this.setVolumeControlStream( AudioManager.STREAM_MUSIC );
      
      setupQueryButtons();
      setupKmlButtons();
  }  
  
  private void setupQueryButtons() {
    Button button = (Button) findViewById( R.id.search_button );
    button.setOnClickListener(new OnClickListener() {
      public void onClick( final View view ) {
        final QueryArgs queryArgs = new QueryArgs();
        String fail = null;
        String field = null;
        boolean okValue = false;
        
        for ( final int id : new int[]{ R.id.query_address, R.id.query_ssid } ) {
          if ( fail != null ) {
            break;
          }
          
          final EditText editText = (EditText) findViewById( id );
          final String text = editText.getText().toString().trim();
          if ( "".equals(text) ) {
            continue;
          }
          
          try {
            switch( id ) {
              case R.id.query_address:
                field = getString(R.string.address);
                Geocoder gc = new Geocoder(DataActivity.this);
                List<Address> addresses = gc.getFromLocationName(text, 1);
                if ( addresses.size() < 1 ) {
                  fail = getString(R.string.no_address_found);
                  break;
                }
                queryArgs.setAddress(addresses.get(0));
                okValue = true;
                break;
              case R.id.query_ssid:
                field = getString(R.string.ssid);
                queryArgs.setSSID(text);
                okValue = true;
                break;
              default:
                ListActivity.error("setupButtons: bad id: " + id);
            }
          }
          catch( Exception ex ) {
            fail = getString(R.string.problem_with_field) + " '" + field + "': " + ex.getMessage();
            break;
          }          
        }
        
        if ( fail == null && ! okValue ) {
          fail = "No query fields specified";
        }
        
        if ( fail != null ) {
          // toast!
          Toast.makeText( DataActivity.this, fail, Toast.LENGTH_SHORT ).show();
        }
        else {
          ListActivity.lameStatic.queryArgs = queryArgs;
          // mark dirty
          final ListActivity listActivity = MainActivity.getListActivity(DataActivity.this);
          if ( listActivity != null ) {
            //listActivity.markDirty();
          }
          MainActivity.switchTab( DataActivity.this, MainActivity.TAB_LIST );
        }
      }
    });
    
    button = (Button) findViewById( R.id.reset_button );
    button.setOnClickListener(new OnClickListener() {
      public void onClick( final View view ) {
        for ( final int id : new int[]{ R.id.query_address, R.id.query_ssid } ) {        
          final EditText editText = (EditText) findViewById( id );
          editText.setText("");
        }
      }
    });

  }
  
  private void setupKmlButtons() {
    final Button kmlRunExportButton = (Button) findViewById( R.id.kml_run_export_button );
    kmlRunExportButton.setOnClickListener( new OnClickListener() {
      public void onClick( final View buttonView ) {  
        MainActivity.createConfirmation( DataActivity.this, "Export run to KML file?", new Doer() {
          @Override
          public void execute() {
            // actually need this Activity context, for dialogs
            KmlWriter kmlWriter = new KmlWriter( DataActivity.this, ListActivity.lameStatic.dbHelper, 
                ListActivity.lameStatic.runNetworks );
            kmlWriter.start();
          }
        } );
      }
    });
    
    final Button kmlExportButton = (Button) findViewById( R.id.kml_export_button );
    kmlExportButton.setOnClickListener( new OnClickListener() {
      public void onClick( final View buttonView ) {  
        MainActivity.createConfirmation( DataActivity.this, "Export DB to KML file?", new Doer() {
          @Override
          public void execute() {
            // actually need this Activity context, for dialogs
            KmlWriter kmlWriter = new KmlWriter( DataActivity.this, ListActivity.lameStatic.dbHelper );
            kmlWriter.start();
          }
        } );
      }
    });    
  }
  
  @Override
  public void onResume() {
    ListActivity.info( "resume data." );    
    super.onResume();
  }
   
  /* Creates the menu items */
  @Override
  public boolean onCreateOptionsMenu( final Menu menu ) {
      MenuItem item = menu.add( 0, MENU_EXIT, 0, getString(R.string.menu_exit) );
      item.setIcon( android.R.drawable.ic_menu_close_clear_cancel );
              
      item = menu.add( 0, MENU_ERROR_REPORT, 0, getString(R.string.menu_error_report) );
      item.setIcon( android.R.drawable.ic_menu_report_image );
      
      item = menu.add( 0, MENU_SETTINGS, 0, getString(R.string.menu_settings) );
      item.setIcon( android.R.drawable.ic_menu_preferences );      
      
      return true;
  }

  /* Handles item selections */
  @Override
  public boolean onOptionsItemSelected( final MenuItem item ) {
      switch ( item.getItemId() ) {
        case MENU_EXIT:
          MainActivity.finishListActivity( this );
          finish();
          return true;
        case MENU_SETTINGS:
          final Intent settingsIntent = new Intent( this, SettingsActivity.class );
          startActivity( settingsIntent );
          break;
        case MENU_ERROR_REPORT:
          final Intent errorReportIntent = new Intent( this, ErrorReportActivity.class );
          startActivity( errorReportIntent );
          break;
      }
      return false;
  }
  
  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_BACK) {
      ListActivity.info( "onKeyDown: not quitting app on back" );
      MainActivity.switchTab( this, MainActivity.TAB_LIST );
      return true;
    }
    return super.onKeyDown(keyCode, event);
  }
  
}
