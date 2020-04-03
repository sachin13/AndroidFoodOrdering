package com.inducesmile.androidfoodordering.Fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.inducesmile.androidfoodordering.R;
import com.inducesmile.androidfoodordering.adapter.HistoryAdapter;
import com.inducesmile.androidfoodordering.entities.HistoryObject;
import com.inducesmile.androidfoodordering.entities.LoginObject;
import com.inducesmile.androidfoodordering.entities.OrderObject;
import com.inducesmile.androidfoodordering.network.GsonRequest;
import com.inducesmile.androidfoodordering.network.VolleySingleton;
import com.inducesmile.androidfoodordering.util.CustomApplication;
import com.inducesmile.androidfoodordering.util.Helper;

import java.util.ArrayList;
import java.util.List;

public class OrderHistoryFragment extends Fragment {

    private static final String TAG = OrderHistoryFragment.class.getSimpleName();

    private RecyclerView historyRecyclerView;

    public OrderHistoryFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getActivity().setTitle(getString(R.string.my_history));
        View view = inflater.inflate(R.layout.fragment_order_history, container, false);

        historyRecyclerView = (RecyclerView)view.findViewById(R.id.order_history);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        historyRecyclerView.setLayoutManager(linearLayoutManager);
        historyRecyclerView.setHasFixedSize(true);

        // get user id from shared preference
        LoginObject loginUser = ((CustomApplication)getActivity().getApplication()).getLoginUser();
        int id = loginUser.getId();
        getOrderHistoriesFromRemoteServer();

        return view;
    }

    private void getOrderHistoriesFromRemoteServer(){
        GsonRequest<HistoryObject[]> serverRequest = new GsonRequest<HistoryObject[]>(
                Request.Method.GET,
                Helper.PATH_TO_ALL_ORDER,
                HistoryObject[].class,
                createRequestSuccessListener(),
                createRequestErrorListener());

        serverRequest.setRetryPolicy(new DefaultRetryPolicy(
                Helper.MY_SOCKET_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        VolleySingleton.getInstance(getActivity()).addToRequestQueue(serverRequest);
    }

    private Response.Listener<HistoryObject[]> createRequestSuccessListener() {
        return new Response.Listener<HistoryObject[]>() {
            @Override
            public void onResponse(HistoryObject[] response) {
                try {
                    Log.d(TAG, "Json Response " + response.toString());
                    List<OrderObject> orderList = new ArrayList<>();
                    if(response.length > 0){
                        for(int i = 0; i < response.length; i++){
                            HistoryObject orderHistory = response[i];
                            int orderId = orderHistory.getOrder_id();
                            String orderDate = orderHistory.getOrder_date();
                            float orderPrice = orderHistory.getOrder_price();
                            String orderStatus = orderHistory.getStatus();
                            orderList.add(new OrderObject(orderId, orderDate, orderPrice, orderStatus));
                        }
                        HistoryAdapter mAdapter = new HistoryAdapter(getActivity(), orderList);
                        historyRecyclerView.setAdapter(mAdapter);

                    }else{
                        Toast.makeText(getActivity(), R.string.failed_login, Toast.LENGTH_LONG).show();
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }

    private Response.ErrorListener createRequestErrorListener() {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        };
    }

}
