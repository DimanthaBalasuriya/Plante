package com.example.plante.Navigation;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.plante.Activities.Treatment;
import com.example.plante.R;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DiseaseRecognition extends AppCompatActivity {
	
	private Button btn_drec_close;
	private TextureView textureView;
	private TextView txv_go_camera_help;
	private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
	
	static {
		ORIENTATIONS.append(Surface.ROTATION_0, 90);
		ORIENTATIONS.append(Surface.ROTATION_90, 0);
		ORIENTATIONS.append(Surface.ROTATION_180, 270);
		ORIENTATIONS.append(Surface.ROTATION_270, 180);
	}
	
	private String cameraID;
	CameraDevice cameraDevice;
	CameraCaptureSession cameraCaptureSession;
	CaptureRequest captureRequest;
	CaptureRequest.Builder captureRequestBuilder;
	
	private Size imageDimensions;
	private ImageReader imageReader;
	private File file;
	Handler mBackgroundHandler;
	HandlerThread mBackgroundThread;
	private Intent intent;
	
	private Button btn_btl_treatment;
	private TextView txv_btl_dname, txv_btl_dcontent;
	private ImageView imv_btl_dimage, imv_btl_dclose;
	
	private RelativeLayout bottomSheetLayout;
	private BottomSheetBehavior sheetBehavior;
	
	private FirebaseAuth mAuth;
	
	SharedPreferences languagesp;
	SharedPreferences.Editor language;
	private static final String SINHALA_FONT = "SINHALA";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_disease_recognition);
		
		ActivityCompat.requestPermissions(DiseaseRecognition.this, new String[]{Manifest.permission.INTERNET}, 2);
		
		mAuth = FirebaseAuth.getInstance();
		
		btn_btl_treatment = findViewById(R.id.btn_btl_treatment);
		imv_btl_dclose = findViewById(R.id.imv_btl_dclose);
		imv_btl_dimage = findViewById(R.id.imv_btl_dimage);
		txv_btl_dname = findViewById(R.id.txv_btl_dname);
		txv_btl_dcontent = findViewById(R.id.txv_btl_dcontent);
		btn_drec_close = findViewById(R.id.btn_drec_close);
		txv_go_camera_help = findViewById(R.id.txv_go_camera_help);
		
		languagesp = getSharedPreferences("Language", MODE_PRIVATE);
		boolean isSinhala = languagesp.getBoolean("" + SINHALA_FONT, false);
		
		if (isSinhala) {
			changeToSinhala();
		} else {
			changeToEnglish();
		}
		
		bottomSheetLayout = findViewById(R.id.rlt_bottom_sheet);
		
		sheetBehavior = BottomSheetBehavior.from(bottomSheetLayout);
		
		sheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
			@Override
			public void onStateChanged(@NonNull View view, int i) {
				switch (i) {
					case BottomSheetBehavior.STATE_HIDDEN:
						break;
					case BottomSheetBehavior.STATE_EXPANDED: {
						Toast.makeText(getApplicationContext(), "Close", Toast.LENGTH_SHORT).show();
					}
					break;
					case BottomSheetBehavior.STATE_COLLAPSED: {
						Toast.makeText(getApplicationContext(), "Expand", Toast.LENGTH_SHORT).show();
					}
					break;
					case BottomSheetBehavior.STATE_DRAGGING:
						break;
					case BottomSheetBehavior.STATE_SETTLING:
						break;
				}
			}
			
			@Override
			public void onSlide(@NonNull View view, float v) {
			
			}
		});
		
		btn_btl_treatment.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String diseaseName = txv_btl_dname.getText().toString().trim();
				DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
				Query query = reference.child("Diseases").orderByChild("name").equalTo(diseaseName);
				query.addValueEventListener(new ValueEventListener() {
					@Override
					public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
						for (DataSnapshot d : dataSnapshot.getChildren()) {
							String did = d.child("diseaseId").getValue(String.class);
							intent = new Intent(getApplicationContext(), Treatment.class);
							intent.putExtra("did", did);
							startActivity(intent);
						}
					}
					
					@Override
					public void onCancelled(@NonNull DatabaseError databaseError) {
					
					}
				});
			}
		});
		
		textureView = findViewById(R.id.texture_view);
		txv_go_camera_help = findViewById(R.id.txv_go_camera_help);
		textureView.setSurfaceTextureListener(textureListener);
		
		textureView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					takePicture();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
		btn_drec_close.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
	}
	
	private void changeToSinhala() {
		btn_btl_treatment.setText(R.string.sinhala_treatments);
		txv_btl_dname.setText(R.string.sinhala_disease);
		txv_go_camera_help.setText(R.string.sinhala_camera_message);
	}
	
	private void changeToEnglish() {
		btn_btl_treatment.setText(R.string.treatment);
		txv_btl_dname.setText(R.string.diseases);
		txv_go_camera_help.setText(R.string.camera_message);
	}
	
	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		if (requestCode == 101) {
			if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
				Toast.makeText(getApplicationContext(), "Sorry, Camera permission is necessary", Toast.LENGTH_SHORT).show();
			}
		}
	}
	
	TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
		@Override
		public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
			try {
				openCamera();
			} catch (CameraAccessException e) {
				e.printStackTrace();
			}
		}
		
		@Override
		public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
		
		}
		
		@Override
		public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
			return false;
		}
		
		@Override
		public void onSurfaceTextureUpdated(SurfaceTexture surface) {
		
		}
	};
	
	private final CameraDevice.StateCallback stateCallBack = new CameraDevice.StateCallback() {
		@Override
		public void onOpened(CameraDevice camera) {
			cameraDevice = camera;
			try {
				createCameraPreview();
			} catch (CameraAccessException e) {
				e.printStackTrace();
			}
		}
		
		@Override
		public void onDisconnected(CameraDevice camera) {
			cameraDevice.close();
		}
		
		@Override
		public void onError(CameraDevice camera, int error) {
			cameraDevice.close();
			cameraDevice = null;
		}
	};
	
	private void createCameraPreview() throws CameraAccessException {
		SurfaceTexture texture = textureView.getSurfaceTexture();
		texture.setDefaultBufferSize(imageDimensions.getWidth(), imageDimensions.getHeight());
		Surface surface = new Surface(texture);
		captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
		captureRequestBuilder.addTarget(surface);
		
		cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
			@Override
			public void onConfigured(@NonNull CameraCaptureSession session) {
				if (cameraDevice == null) {
					return;
				}
				cameraCaptureSession = session;
				try {
					updatePreview();
				} catch (CameraAccessException e) {
					e.printStackTrace();
				}
			}
			
			@Override
			public void onConfigureFailed(@NonNull CameraCaptureSession session) {
				Toast.makeText(getApplicationContext(), "Configuration Changed!", Toast.LENGTH_SHORT).show();
			}
		}, null);
		
	}
	
	private void updatePreview() throws CameraAccessException {
		if (cameraDevice == null) {
			return;
		}
		captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
		cameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(), null, mBackgroundHandler);
	}
	
	private void openCamera() throws CameraAccessException {
		CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
		cameraID = manager.getCameraIdList()[0];
		CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraID);
		StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
		imageDimensions = map.getOutputSizes(SurfaceTexture.class)[0];
		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(DiseaseRecognition.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 101);
			return;
		}
		
		manager.openCamera(cameraID, stateCallBack, null);
	}
	
	private void takePicture() throws CameraAccessException {
		if (cameraDevice == null) {
			return;
		}
		
		CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
		CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraDevice.getId());
		Size[] jpegSizes = null;
		jpegSizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(ImageFormat.JPEG);
		int width = 640;
		int height = 480;
		
		if (jpegSizes != null && jpegSizes.length > 0) {
			width = jpegSizes[0].getWidth();
			height = jpegSizes[0].getHeight();
		}
		
		ImageReader reader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1);
		List<Surface> outSurfaces = new ArrayList<>(2);
		outSurfaces.add(reader.getSurface());
		outSurfaces.add(new Surface(textureView.getSurfaceTexture()));
		final CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
		captureBuilder.addTarget(reader.getSurface());
		captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
		int rotation = getWindowManager().getDefaultDisplay().getRotation();
		captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));
		
		//file = new File();
		ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {
			@Override
			public void onImageAvailable(ImageReader reader) {
				Image image = null;
				image = reader.acquireLatestImage();
				System.out.println(image);
				ByteBuffer buffer = image.getPlanes()[0].getBuffer();
				byte[] bytes = new byte[buffer.remaining()];
				buffer.get(bytes);
				try {
					Long tsLong = System.currentTimeMillis() / 1000;
					String ts = tsLong.toString();
					OutputStream outputStream = null;
					//Detect disease
					outputStream = new FileOutputStream(Environment.getExternalStorageDirectory() + "/" + ts + ".jpg");
					outputStream.write(bytes);
					outputStream.close();
					connectToServer(Environment.getExternalStorageDirectory() + "/" + ts + ".jpg");
					buffer.clear();
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					if (image != null) {
						image.close();
					}
				}
			}
		};
		
		reader.setOnImageAvailableListener(readerListener, mBackgroundHandler);
		
		final CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {
			@Override
			public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
				super.onCaptureCompleted(session, request, result);
				Toast.makeText(getApplicationContext(), "Saved", Toast.LENGTH_SHORT).show();
				try {
					createCameraPreview();
				} catch (CameraAccessException e) {
					e.printStackTrace();
				}
			}
		};
		
		cameraDevice.createCaptureSession(outSurfaces, new CameraCaptureSession.StateCallback() {
			@Override
			public void onConfigured(@NonNull CameraCaptureSession session) {
				try {
					session.capture(captureBuilder.build(), captureListener, mBackgroundHandler);
				} catch (CameraAccessException e) {
					e.printStackTrace();
				}
			}
			
			@Override
			public void onConfigureFailed(@NonNull CameraCaptureSession session) {
			
			}
		}, mBackgroundHandler);
		
	}
	
	private void connectToServer(String imagePath) {
		String ip = "10.0.2.2";
		int port = 5000;
		
		String url = "http://" + ip + ":" + port + "/";
		
		System.out.println("IMGURL" + imagePath);
		
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inPreferredConfig = Bitmap.Config.RGB_565;
		Bitmap bitmap = BitmapFactory.decodeFile(imagePath, options);
		bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
		byte[] byteArray = stream.toByteArray();
		
		RequestBody postBodyImage = new MultipartBody.Builder().setType(MultipartBody.FORM).addFormDataPart("image", "androidFlask.jpg", RequestBody.create(MediaType.parse("image/*jpg"), byteArray)).build();
		
		postRequest(url, postBodyImage);
	}
	
	private void postRequest(String url, RequestBody postBodyImage) {
		OkHttpClient client = new OkHttpClient();
		Request request = new Request.Builder().url(url).post(postBodyImage).build();
		client.newCall(request).enqueue(new Callback() {
			@Override
			public void onFailure(Call call, IOException e) {
				call.cancel();
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(getApplicationContext(), "Faild to connect server", Toast.LENGTH_SHORT).show();
					}
				});
			}
			
			@Override
			public void onResponse(Call call, Response response) throws IOException {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						try {
							String disease = response.body().string();
							DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
							Query query = reference.child("Diseases").orderByChild("name").equalTo(disease + "");
							query.addValueEventListener(new ValueEventListener() {
								@Override
								public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
									for (DataSnapshot d : dataSnapshot.getChildren()) {
										String name = d.child("name").getValue(String.class);
										String content = d.child("description").getValue(String.class);
										String image = d.child("image").getValue(String.class);
										txv_btl_dname.setText(name + "");
										txv_btl_dcontent.setText(content + "");
										
										if (image != null) {
											try {
												imv_btl_dimage.setVisibility(View.VISIBLE);
												Picasso.get().load(image).placeholder(R.drawable.ic_image).into(imv_btl_dimage);
											} catch (Exception e) {
												e.printStackTrace();
											}
										} else {
											imv_btl_dimage.setVisibility(View.GONE);
										}
									}
								}
								
								@Override
								public void onCancelled(@NonNull DatabaseError databaseError) {
								
								}
							});
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				});
			}
		});
	}
	
	private void save(byte[] bytes) throws IOException {
	
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		startBackgroundThread();
		if (textureView.isAvailable()) {
			try {
				openCamera();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			textureView.setSurfaceTextureListener(textureListener);
		}
	}
	
	private void startBackgroundThread() {
		mBackgroundThread = new HandlerThread("Camera Background");
		mBackgroundThread.start();
		mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
	}
	
	@Override
	protected void onPause() {
		try {
			stopBackgroundThread();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		super.onPause();
	}
	
	private void stopBackgroundThread() throws InterruptedException {
		mBackgroundThread.quitSafely();
		mBackgroundThread.join();
		mBackgroundThread = null;
		mBackgroundHandler = null;
	}
	
}
