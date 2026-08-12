package com.fantonio.entregarg.data.local;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.fantonio.entregarg.data.model.Identity;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class IdentityDao_Impl implements IdentityDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<Identity> __insertionAdapterOfIdentity;

  private final EntityDeletionOrUpdateAdapter<Identity> __updateAdapterOfIdentity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteAll;

  public IdentityDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfIdentity = new EntityInsertionAdapter<Identity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `identities` (`id`,`nome`,`cpf`,`lote`,`retirada`,`retiradaPor`,`retiradaData`) VALUES (nullif(?, 0),?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Identity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getNome());
        statement.bindString(3, entity.getCpf());
        statement.bindString(4, entity.getLote());
        final int _tmp = entity.getRetirada() ? 1 : 0;
        statement.bindLong(5, _tmp);
        if (entity.getRetiradaPor() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getRetiradaPor());
        }
        if (entity.getRetiradaData() == null) {
          statement.bindNull(7);
        } else {
          statement.bindLong(7, entity.getRetiradaData());
        }
      }
    };
    this.__updateAdapterOfIdentity = new EntityDeletionOrUpdateAdapter<Identity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `identities` SET `id` = ?,`nome` = ?,`cpf` = ?,`lote` = ?,`retirada` = ?,`retiradaPor` = ?,`retiradaData` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Identity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getNome());
        statement.bindString(3, entity.getCpf());
        statement.bindString(4, entity.getLote());
        final int _tmp = entity.getRetirada() ? 1 : 0;
        statement.bindLong(5, _tmp);
        if (entity.getRetiradaPor() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getRetiradaPor());
        }
        if (entity.getRetiradaData() == null) {
          statement.bindNull(7);
        } else {
          statement.bindLong(7, entity.getRetiradaData());
        }
        statement.bindLong(8, entity.getId());
      }
    };
    this.__preparedStmtOfDeleteAll = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM identities";
        return _query;
      }
    };
  }

  @Override
  public Object insertAll(final List<Identity> identities,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfIdentity.insert(identities);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateIdentity(final Identity identity,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfIdentity.handle(identity);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteAll(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteAll.acquire();
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteAll.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object searchIdentities(final String query,
      final Continuation<? super List<Identity>> $completion) {
    final String _sql = "SELECT * FROM identities WHERE nome LIKE '%' || ? || '%' OR cpf = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindString(_argIndex, query);
    _argIndex = 2;
    _statement.bindString(_argIndex, query);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<Identity>>() {
      @Override
      @NonNull
      public List<Identity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfNome = CursorUtil.getColumnIndexOrThrow(_cursor, "nome");
          final int _cursorIndexOfCpf = CursorUtil.getColumnIndexOrThrow(_cursor, "cpf");
          final int _cursorIndexOfLote = CursorUtil.getColumnIndexOrThrow(_cursor, "lote");
          final int _cursorIndexOfRetirada = CursorUtil.getColumnIndexOrThrow(_cursor, "retirada");
          final int _cursorIndexOfRetiradaPor = CursorUtil.getColumnIndexOrThrow(_cursor, "retiradaPor");
          final int _cursorIndexOfRetiradaData = CursorUtil.getColumnIndexOrThrow(_cursor, "retiradaData");
          final List<Identity> _result = new ArrayList<Identity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Identity _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpNome;
            _tmpNome = _cursor.getString(_cursorIndexOfNome);
            final String _tmpCpf;
            _tmpCpf = _cursor.getString(_cursorIndexOfCpf);
            final String _tmpLote;
            _tmpLote = _cursor.getString(_cursorIndexOfLote);
            final boolean _tmpRetirada;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfRetirada);
            _tmpRetirada = _tmp != 0;
            final String _tmpRetiradaPor;
            if (_cursor.isNull(_cursorIndexOfRetiradaPor)) {
              _tmpRetiradaPor = null;
            } else {
              _tmpRetiradaPor = _cursor.getString(_cursorIndexOfRetiradaPor);
            }
            final Long _tmpRetiradaData;
            if (_cursor.isNull(_cursorIndexOfRetiradaData)) {
              _tmpRetiradaData = null;
            } else {
              _tmpRetiradaData = _cursor.getLong(_cursorIndexOfRetiradaData);
            }
            _item = new Identity(_tmpId,_tmpNome,_tmpCpf,_tmpLote,_tmpRetirada,_tmpRetiradaPor,_tmpRetiradaData);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<Identity>> getAllIdentities() {
    final String _sql = "SELECT * FROM identities";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"identities"}, new Callable<List<Identity>>() {
      @Override
      @NonNull
      public List<Identity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfNome = CursorUtil.getColumnIndexOrThrow(_cursor, "nome");
          final int _cursorIndexOfCpf = CursorUtil.getColumnIndexOrThrow(_cursor, "cpf");
          final int _cursorIndexOfLote = CursorUtil.getColumnIndexOrThrow(_cursor, "lote");
          final int _cursorIndexOfRetirada = CursorUtil.getColumnIndexOrThrow(_cursor, "retirada");
          final int _cursorIndexOfRetiradaPor = CursorUtil.getColumnIndexOrThrow(_cursor, "retiradaPor");
          final int _cursorIndexOfRetiradaData = CursorUtil.getColumnIndexOrThrow(_cursor, "retiradaData");
          final List<Identity> _result = new ArrayList<Identity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Identity _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpNome;
            _tmpNome = _cursor.getString(_cursorIndexOfNome);
            final String _tmpCpf;
            _tmpCpf = _cursor.getString(_cursorIndexOfCpf);
            final String _tmpLote;
            _tmpLote = _cursor.getString(_cursorIndexOfLote);
            final boolean _tmpRetirada;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfRetirada);
            _tmpRetirada = _tmp != 0;
            final String _tmpRetiradaPor;
            if (_cursor.isNull(_cursorIndexOfRetiradaPor)) {
              _tmpRetiradaPor = null;
            } else {
              _tmpRetiradaPor = _cursor.getString(_cursorIndexOfRetiradaPor);
            }
            final Long _tmpRetiradaData;
            if (_cursor.isNull(_cursorIndexOfRetiradaData)) {
              _tmpRetiradaData = null;
            } else {
              _tmpRetiradaData = _cursor.getLong(_cursorIndexOfRetiradaData);
            }
            _item = new Identity(_tmpId,_tmpNome,_tmpCpf,_tmpLote,_tmpRetirada,_tmpRetiradaPor,_tmpRetiradaData);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getAllList(final Continuation<? super List<Identity>> $completion) {
    final String _sql = "SELECT * FROM identities";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<Identity>>() {
      @Override
      @NonNull
      public List<Identity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfNome = CursorUtil.getColumnIndexOrThrow(_cursor, "nome");
          final int _cursorIndexOfCpf = CursorUtil.getColumnIndexOrThrow(_cursor, "cpf");
          final int _cursorIndexOfLote = CursorUtil.getColumnIndexOrThrow(_cursor, "lote");
          final int _cursorIndexOfRetirada = CursorUtil.getColumnIndexOrThrow(_cursor, "retirada");
          final int _cursorIndexOfRetiradaPor = CursorUtil.getColumnIndexOrThrow(_cursor, "retiradaPor");
          final int _cursorIndexOfRetiradaData = CursorUtil.getColumnIndexOrThrow(_cursor, "retiradaData");
          final List<Identity> _result = new ArrayList<Identity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Identity _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpNome;
            _tmpNome = _cursor.getString(_cursorIndexOfNome);
            final String _tmpCpf;
            _tmpCpf = _cursor.getString(_cursorIndexOfCpf);
            final String _tmpLote;
            _tmpLote = _cursor.getString(_cursorIndexOfLote);
            final boolean _tmpRetirada;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfRetirada);
            _tmpRetirada = _tmp != 0;
            final String _tmpRetiradaPor;
            if (_cursor.isNull(_cursorIndexOfRetiradaPor)) {
              _tmpRetiradaPor = null;
            } else {
              _tmpRetiradaPor = _cursor.getString(_cursorIndexOfRetiradaPor);
            }
            final Long _tmpRetiradaData;
            if (_cursor.isNull(_cursorIndexOfRetiradaData)) {
              _tmpRetiradaData = null;
            } else {
              _tmpRetiradaData = _cursor.getLong(_cursorIndexOfRetiradaData);
            }
            _item = new Identity(_tmpId,_tmpNome,_tmpCpf,_tmpLote,_tmpRetirada,_tmpRetiradaPor,_tmpRetiradaData);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
