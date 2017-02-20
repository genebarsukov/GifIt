package codewrencher.gifit.ui.help;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;

import codewrencher.gifit.R;

public class HelpActivity extends AppCompatActivity {

    public HelpFragmentPager fragment_pager;
    public ViewPager view_pager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        int page_count = 7;
        fragment_pager = new HelpFragmentPager(getSupportFragmentManager(), page_count);
        view_pager = (ViewPager) findViewById(R.id.pager);
        view_pager.setAdapter(fragment_pager);
    }

    public void switchTab(int tab_index) {
        view_pager.setCurrentItem(tab_index);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_help, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public int getResourceId(String resource_name, String resource_type)
    {
        try {
            return getResources().getIdentifier(resource_name, resource_type, getPackageName());
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
}
