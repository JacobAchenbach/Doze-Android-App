package cache.doze.Tools;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

import cache.doze.Model.ReplyListAdapter;

/**
 * Created by Chris on 1/12/2019.
 */

public class ItemMoveCallback extends ItemTouchHelper.Callback {
    private Context context;
    private final ItemTouchHelperContract mAdapter;

    public ItemMoveCallback(ItemTouchHelperContract adapter) {
        mAdapter = adapter;
        this.context = ((ReplyListAdapter)mAdapter).context;
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return false;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return false;
    }



    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {

    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
        return makeMovementFlags(dragFlags, 0);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                          RecyclerView.ViewHolder target) {
        mAdapter.onRowMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());
        return true;
    }

    RecyclerView.ViewHolder lastViewHolder;

    @Override
    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
        ReplyListAdapter replyListAdapter = (ReplyListAdapter)mAdapter;
        if(actionState == ItemTouchHelper.ACTION_STATE_IDLE){
            //replyListAdapter.fixRecyclerView();
            if(lastViewHolder != null)
                replyListAdapter.changeViewHolderElevation((ReplyListAdapter.ViewHolder)lastViewHolder, QuickTools.convertDpToPx(context, 2) + 5);
        }

        RecyclerView.ViewHolder holder;
        if (viewHolder != null) {
            holder = (RecyclerView.ViewHolder) viewHolder;
        }else return;

        if(lastViewHolder != null)
            ((ReplyListAdapter)mAdapter).changeViewHolderElevation((ReplyListAdapter.ViewHolder)lastViewHolder, QuickTools.convertDpToPx(context, 2));
        lastViewHolder = holder;

        if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
                mAdapter.onRowSelected(holder);
        }

        super.onSelectedChanged(viewHolder, actionState);
    }


    public interface ItemTouchHelperContract {

        void onRowMoved(int fromPosition, int toPosition);
        void onRowSelected(RecyclerView.ViewHolder myViewHolder);
        void onRowClear(RecyclerView.ViewHolder myViewHolder);

    }

}
