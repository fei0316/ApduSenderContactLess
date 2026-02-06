/*
Copyright 2014  Jose Maria ARROYO jm.arroyo.castejon@gmail.com

APDUSenderContactLess is free software: you can redistribute it and/or modify
it  under  the  terms  of the GNU General Public License  as published by the 
Free Software Foundation, either version 3 of the License, or (at your option) 
any later version.

APDUSenderContactLess is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.

*/

package com.jmarroyo.apdusendercontactless;


import java.io.IOException;
import java.util.Arrays;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.Parcelable;

public class ApduSenderContactLess extends Activity
{

    static byte[] byteAPDU=null;
    static byte[] respAPDU=null;

    static HexadecimalKbd mHexKbd;

    private static CheckBox mCheckRaw;

    private Button mSendAPDUButton;
    private Button mSetNFCButton;
    private Button mPasteButton;

    static ImageView icoNfc;
    static ImageView icoCard;
    
    static TextView TextNfc;
    static TextView TextCard;
    static TextView txtDataIn;
    static EditText editDataIn;
    static TextView txtLog;
    
    private Spinner mCommandsSpinner;

    private NfcAdapter mAdapter=null;
    private PendingIntent mPendingIntent;
    private String[][] mTechLists;
    private IntentFilter[] mFilters;
    static IsoDep myTag;
    boolean mFirstDetected=false;
    boolean mShowAtr=false;



    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        byteAPDU=null;
        respAPDU=null;

        mHexKbd= new HexadecimalKbd(this, R.id.keyboardview, R.xml.hexkbd );
        mHexKbd.registerEditText(R.id.editDataIn);

        txtLog = (TextView) findViewById(R.id.textLog);
        icoNfc = (ImageView) findViewById(R.id.imageNfc);
        icoNfc.setImageResource(R.drawable.ic_nfc_off);
        icoCard = (ImageView) findViewById(R.id.imageCard);
        icoCard.setImageResource(R.drawable.ic_icc_off);
        TextNfc = (TextView) findViewById(R.id.textNfc);
        TextCard = (TextView) findViewById(R.id.textCard);

        txtDataIn = (TextView) findViewById(R.id.textDataIn);
        editDataIn = (EditText) findViewById(R.id.editDataIn);

        mSendAPDUButton = (Button) findViewById(R.id.button_SendApdu);
        mSendAPDUButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if( mFirstDetected==true && myTag.isConnected())
                {
                    if(mShowAtr==true)
                    {
                        icoCard.setImageResource(R.drawable.ic_icc_on_atr);
                    }
                    else
                    {
                        icoCard.setImageResource(R.drawable.ic_icc_on);
                    }
                    clearlog();
                    if(!bSendAPDU())
                    {
                        vShowErrorVaules();
                    }
                }
                else
                {
                    icoCard.setImageResource(R.drawable.ic_icc_off);
                    clearlog();
                    TextCard.setText("PLEASE TAP CARD");
                    editDataIn.setText("");
                    mSendAPDUButton.setEnabled(false);
                }
            }
        });

        mSetNFCButton = (Button) findViewById(R.id.button_SetNFC);
        mSetNFCButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                clipboard.setText(txtLog.getText());
            }
        });

        mPasteButton = (Button) findViewById(R.id.button_Paste);
        mPasteButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v) 
            {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE); 
                String ClipBoardData = clipboard.getText().toString().toUpperCase();
                
                if(ClipBoardData.length() > 254)
                {
                    vShowGeneralMesg("Max Length to Paste is 254 chars !");
                }
                else if(ClipBoardData.length() >= 8)
                {
                    if( (ClipBoardData.length()%2)!=0)
                    {
                        vShowGeneralMesg("String Length must be Even !");
                    }
                    if (!ClipBoardData.matches("^[0-9A-F]+$"))
                    {
                        clearlog();
                        print(ClipBoardData);
                        vShowGeneralMesg("String should be '0'-'9' or 'A'-'F'");
                    }
                    else
                    {
                        vSetBuiltinCommand();
                        editDataIn.setText(ClipBoardData);
                        HideKbd();
                        vShowGeneralMesg("Data Pasted Successfully");
                    }
                }
                else
                {
                    vShowGeneralMesg("Length must be greater than 8 chars !");
                }
            }
        });
        
        mCheckRaw = (CheckBox) findViewById(R.id.check_box_raw);
        mCheckRaw.setOnCheckedChangeListener(new OnCheckedChangeListener()
        {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if( mFirstDetected==true && myTag.isConnected() );
                else
                {
                    icoCard.setImageResource(R.drawable.ic_icc_off);
                    clearlog();
                    mSendAPDUButton.setEnabled(false);
                    TextCard.setText("PLEASE TAP CARD");
                }
                if( isChecked )
                {
                    editDataIn.setText("");
                }
                else
                {
                    editDataIn.setText("");
                    txtDataIn.setEnabled(true);
                    editDataIn.setEnabled(true);
                    txtDataIn.setText("Data:");
                    mCommandsSpinner.setSelection(0);
                }
            }
        });
        
        
        final String[] commadsTableNames = { 
            "選擇命令：",
            "第一步 (Select PSE)",
            "第二步 (Select 0x1001)",
            "第三步 (READ SFI 0x15)",
            "第四步 (READ SFI 0x18)",
            "第五步 (READ SFI 0x17)"
            };
        ArrayAdapter<String> commadsTable = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, commadsTableNames);
        mCommandsSpinner = (Spinner) findViewById(R.id.APDU_spinner_table);
        mCommandsSpinner.setAdapter(commadsTable);
        mCommandsSpinner.setSelection(0);
        mCommandsSpinner.setOnItemSelectedListener(new OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,int arg2, long arg3)
            {
                
                int CommandAPDU = mCommandsSpinner.getSelectedItemPosition();
                switch (CommandAPDU)
                {
                    case 0:
                    	
                    mCheckRaw.setChecked(false);
                    editDataIn.setText("");
                    txtDataIn.setEnabled(true);
                    editDataIn.setEnabled(true);
                    txtDataIn.setText("Data:");
                    
                    break;
                    case 1: //SELECT PSE
                        vSetBuiltinCommand();
                        editDataIn.setText("00A404000E315041592E5359532E4444463031");
                        HideKbd();
                        vShowGeneralMesg("Payment System Environment");
                    break;
                    case 2: //SELECT PPSE
                        vSetBuiltinCommand();
                        editDataIn.setText("00a40000021001");
                        HideKbd();
                        vShowGeneralMesg("選擇澳門通0x1001檔案");
                    break;
                    case 3: //SELECT VISA AID
                        vSetBuiltinCommand();
                        editDataIn.setText("00b0950000");
                        HideKbd();
                        vShowGeneralMesg("讀取SFI 0x15");
                    break;
                    case 4: //SELECT VISA ELECTRON AID
                        vSetBuiltinCommand();
                        editDataIn.setText("00b201c5");
                        HideKbd();
                        vShowGeneralMesg("讀取 SFI 0x18");
                    break;
                    case 5: //SELECT MASTERCARD AID
                        vSetBuiltinCommand();
                        editDataIn.setText("00b201bd");
                        HideKbd();
                        vShowGeneralMesg("讀取 SFI 0x17");
                    break; 
                    default:
                    break;
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0)
            {
                return;
            }
        });

     
        mSendAPDUButton.setEnabled(false);
        mSetNFCButton.setEnabled(true);
        mCommandsSpinner.setEnabled(true);

        editDataIn.setText("");
        txtDataIn.setEnabled(true);
        editDataIn.setEnabled(true);
        txtDataIn.setText("Data:");

        resolveIntent(getIntent());

        mAdapter = NfcAdapter.getDefaultAdapter(this);
        mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        try
        {
            ndef.addDataType("*/*");
        }
        catch (MalformedMimeTypeException e)
        {
            throw new RuntimeException("fail", e);
        }
        mFilters = new IntentFilter[] { ndef, };
        mTechLists = new String[][] { new String[] { IsoDep.class.getName() } };
    }


    @Override
    public void onResume()
    {
        super.onResume();

        byteAPDU=null;
        respAPDU=null;

        if(  (mFirstDetected==true) && (myTag.isConnected()) )
        {
            if(mShowAtr==true)
            {
                icoCard.setImageResource(R.drawable.ic_icc_on_atr);
            }
            else
            {
                icoCard.setImageResource(R.drawable.ic_icc_on);
            }
        }
        else
        {
            icoCard.setImageResource(R.drawable.ic_icc_off);
        }
        if( (mAdapter == null) || (!mAdapter.isEnabled()) )
        {
            if (mAdapter == null)
            {
                clearlog();
                TextCard.setText("PLEASE TAP CARD");
                mSendAPDUButton.setEnabled(false);
                mSetNFCButton.setEnabled(false);
                editDataIn.setText("");
                print("    No NFC hardware found.");
                print("    Program will NOT function.");
            }
            else if(mAdapter.isEnabled())
            {
                clearlog();
                TextNfc.setText("NFC ENABLED");
            }
            else
            {
                clearlog();
                TextCard.setText("PLEASE TAP CARD");
                editDataIn.setText("");
                mSendAPDUButton.setEnabled(false);
                print("    NFC hardware has been disabled.");
                print("    Please enable it first.");
                mSetNFCButton.setEnabled(true);
                icoNfc.setImageResource(R.drawable.ic_nfc_off);
                TextNfc.setText("NO READER DETECTED");
            }
        }
        if (mAdapter != null)
        {
            if (mAdapter.isEnabled())
            {
                clearlog();
                TextNfc.setText("NFC ENABLED");
                icoNfc.setImageResource(R.drawable.ic_nfc_on);
                print("This program is distributed in the hope that it will be useful for educational purposes.  Enjoy! ");
            }
            mAdapter.enableForegroundDispatch(this, mPendingIntent, mFilters, mTechLists);
        }
        else
        {
            clearlog();
            icoNfc.setImageResource(R.drawable.ic_nfc_off);
            TextNfc.setText("NO READER DETECTED");
            TextCard.setText("PLEASE TAP CARD");
            mSendAPDUButton.setEnabled(false);
            mSetNFCButton.setEnabled(false);
            editDataIn.setText("");
            print("    No NFC hardware found.");
            print("    Program will NOT function.");
        }
    }

    @Override
    public void onPause()
    {
        super.onPause();
        
        byteAPDU=null;
        respAPDU=null;
        
        if( (mFirstDetected==true) && (myTag.isConnected()) )
        {
            if(mShowAtr==true)
            {
                icoCard.setImageResource(R.drawable.ic_icc_on_atr);
            }
            else
            {
                icoCard.setImageResource(R.drawable.ic_icc_on);
            }
        }
        else
        {
            icoCard.setImageResource(R.drawable.ic_icc_off);
        }
        mAdapter.disableForegroundDispatch(this);
    }

    @Override
    protected void onNewIntent(Intent intent)
    {
        setIntent(intent);
        resolveIntent(intent);
    }

    @Override public void onBackPressed()
    {
        if( mHexKbd.isCustomKeyboardVisible() ) mHexKbd.hideCustomKeyboard(); else this.finish();
    }
    
    public static void HideKbd() 
    {
        if( mHexKbd.isCustomKeyboardVisible() ) mHexKbd.hideCustomKeyboard(); 
    }

    private static void clearlog()
    {
        txtLog.setText("");
    }

    private static void print(String s) 
    {
        txtLog.append(s);
        txtLog.append("\r\n");
        return;
    }

    private static byte[]  transceives (byte[] data)
    {
        byte[] ra = null;
        
        try 
        {
            print("***COMMAND APDU***");
            print("");
            print("IFD - " + getHexString(data));
        } 
        catch (Exception e1) 
        {
            e1.printStackTrace();
        }

        try 
        {
            ra = myTag.transceive(data);
        }
        catch (IOException e)
        {

            print("************************************");
            print("         NO CARD RESPONSE");
            print("************************************");

        }
        try
        {
            print("");
            print("ICC - " + getHexString(ra));
        }
        catch (Exception e1) 
        {
            e1.printStackTrace();
        }

        return (ra);
    }
      
    private static boolean bSendAPDU() 
    {
        HideKbd();

        String StringAPDU = editDataIn.getText().toString();
        if ( ((StringAPDU.length()%2)!=0)|| (StringAPDU.length()<1) )
        {
            return false;
        }

        byteAPDU = atohex(StringAPDU);
        respAPDU = transceives(byteAPDU);

        return true;
    }

    private void resolveIntent(Intent intent) 
    {
        String action = intent.getAction();
        clearlog();
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action) || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action) || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action))
        {
            Parcelable tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            final Tag t = (Tag) tag;
            myTag = IsoDep.get(t);
            mFirstDetected=true;
            if( !myTag.isConnected() )
            {
                try
                {
                    myTag.connect();
                    myTag.setTimeout(5000);
                }
                catch (IOException e) 
                {
                    e.printStackTrace();
                    return;
                }
            }
            if( myTag.isConnected() )
            {
                if(mShowAtr==true)
                {
                    icoCard.setImageResource(R.drawable.ic_icc_on_atr);
                }
                else
                {
                    icoCard.setImageResource(R.drawable.ic_icc_on);
                }
                vShowCardRemovalInfo();
                String szATR = null;
                try
                {
                    mShowAtr=true;
                    szATR =" 3B " + getATRLeString(myTag.getHistoricalBytes())+ "80 01 " + getHexString(myTag.getHistoricalBytes())+""+ getATRXorString(myTag.getHistoricalBytes());
                } 
                catch (Exception e) 
                {
                    mShowAtr=false;
                    szATR = "CARD DETECTED  ";
                }
                TextCard.setText(szATR);

                mSendAPDUButton.setEnabled(true);
                clearlog();
                txtDataIn.setEnabled(true);
                editDataIn.setEnabled(true);
                editDataIn.setText("");
                mCheckRaw.setChecked(false);
            }
            else
            {
                icoCard.setImageResource(R.drawable.ic_icc_off);
            }
        }
        if( mFirstDetected==true && myTag.isConnected() ) 
        {
            if(mShowAtr==true)
            {
                icoCard.setImageResource(R.drawable.ic_icc_on_atr);
            }
            else
            {
                icoCard.setImageResource(R.drawable.ic_icc_on);
            }
        }
        else
        {
            icoCard.setImageResource(R.drawable.ic_icc_off);
        }
    }

    private void vSetBuiltinCommand()
    {
    	clearlog();

        editDataIn.setText("");
        editDataIn.setEnabled(true);
        txtDataIn.setEnabled(true);
        txtDataIn.setText("APDU:");
        mCheckRaw.setChecked(true);
               
        return;
    }

    private void vShowCardRemovalInfo()
    {
        Context context = getApplicationContext();
        CharSequence text = "Card Removal will NOT be detected";
        int duration = Toast.LENGTH_LONG;
        HideKbd();
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

    private void vShowGeneralMesg(String szText)
    {
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_LONG;
        Toast toast = Toast.makeText(context, szText, duration);
        toast.show();
    }

    private void vShowErrorVaules()
    {
        Context context = getApplicationContext();
        CharSequence text = "C-APDU values ERROR";
        int duration = Toast.LENGTH_LONG;
        HideKbd();
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

    private static String getHexString(byte[] data) throws Exception
    {
        String szDataStr = "";
        for (int ii=0; ii < data.length; ii++) 
        {
            szDataStr += String.format("%02X ", data[ii] & 0xFF);
        }
        return szDataStr;
    }

    private static String getATRLeString(byte[] data) throws Exception
    {
        return String.format("%02X ", data.length | 0x80);
    }

    private static String getATRXorString(byte[] b) throws Exception
    {
        int Lrc=0x00;
        Lrc = b.length | 0x80;
        Lrc = Lrc^0x81;
        for (int i=0; i < b.length; i++) 
        {
            Lrc = Lrc^(b[i] & 0xFF);
        }
        return String.format("%02X ", Lrc);
    }

    private static byte[] atohex(String data)
    {
        String hexchars = "0123456789abcdef";

        data = data.replaceAll(" ","").toLowerCase();
        if (data == null)
        {
            return null;
        }
        byte[] hex = new byte[data.length() / 2];
        
        for (int ii = 0; ii < data.length(); ii += 2)
        {
            int i1 = hexchars.indexOf(data.charAt(ii));
            int i2 = hexchars.indexOf(data.charAt(ii + 1));
            hex[ii/2] = (byte)((i1 << 4) | i2);
        }
        return hex;
    }

}

