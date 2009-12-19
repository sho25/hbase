begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|regionserver
operator|.
name|wal
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|Field
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|conf
operator|.
name|Configuration
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|fs
operator|.
name|FSDataOutputStream
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|fs
operator|.
name|FileSystem
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|fs
operator|.
name|Path
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|KeyValue
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|regionserver
operator|.
name|wal
operator|.
name|HLog
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|io
operator|.
name|SequenceFile
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|io
operator|.
name|SequenceFile
operator|.
name|Metadata
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|io
operator|.
name|compress
operator|.
name|DefaultCodec
import|;
end_import

begin_class
specifier|public
class|class
name|SequenceFileLogWriter
implements|implements
name|HLog
operator|.
name|Writer
block|{
name|SequenceFile
operator|.
name|Writer
name|writer
decl_stmt|;
name|FSDataOutputStream
name|writer_out
decl_stmt|;
specifier|public
name|SequenceFileLogWriter
parameter_list|()
block|{ }
annotation|@
name|Override
specifier|public
name|void
name|init
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|Path
name|path
parameter_list|,
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
name|writer
operator|=
name|SequenceFile
operator|.
name|createWriter
argument_list|(
name|fs
argument_list|,
name|conf
argument_list|,
name|path
argument_list|,
name|HLog
operator|.
name|getKeyClass
argument_list|(
name|conf
argument_list|)
argument_list|,
name|KeyValue
operator|.
name|class
argument_list|,
name|fs
operator|.
name|getConf
argument_list|()
operator|.
name|getInt
argument_list|(
literal|"io.file.buffer.size"
argument_list|,
literal|4096
argument_list|)
argument_list|,
operator|(
name|short
operator|)
name|conf
operator|.
name|getInt
argument_list|(
literal|"hbase.regionserver.hlog.replication"
argument_list|,
name|fs
operator|.
name|getDefaultReplication
argument_list|()
argument_list|)
argument_list|,
name|conf
operator|.
name|getLong
argument_list|(
literal|"hbase.regionserver.hlog.blocksize"
argument_list|,
name|fs
operator|.
name|getDefaultBlockSize
argument_list|()
argument_list|)
argument_list|,
name|SequenceFile
operator|.
name|CompressionType
operator|.
name|NONE
argument_list|,
operator|new
name|DefaultCodec
argument_list|()
argument_list|,
literal|null
argument_list|,
operator|new
name|Metadata
argument_list|()
argument_list|)
expr_stmt|;
comment|// Get at the private FSDataOutputStream inside in SequenceFile so we can
comment|// call sync on it.  Make it accessible.  Stash it aside for call up in
comment|// the sync method.
specifier|final
name|Field
name|fields
index|[]
init|=
name|writer
operator|.
name|getClass
argument_list|()
operator|.
name|getDeclaredFields
argument_list|()
decl_stmt|;
specifier|final
name|String
name|fieldName
init|=
literal|"out"
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|fields
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
if|if
condition|(
name|fieldName
operator|.
name|equals
argument_list|(
name|fields
index|[
name|i
index|]
operator|.
name|getName
argument_list|()
argument_list|)
condition|)
block|{
try|try
block|{
name|fields
index|[
name|i
index|]
operator|.
name|setAccessible
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|this
operator|.
name|writer_out
operator|=
operator|(
name|FSDataOutputStream
operator|)
name|fields
index|[
name|i
index|]
operator|.
name|get
argument_list|(
name|writer
argument_list|)
expr_stmt|;
break|break;
block|}
catch|catch
parameter_list|(
name|IllegalAccessException
name|ex
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Accessing "
operator|+
name|fieldName
argument_list|,
name|ex
argument_list|)
throw|;
block|}
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|append
parameter_list|(
name|HLog
operator|.
name|Entry
name|entry
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|writer
operator|.
name|append
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|entry
operator|.
name|getEdit
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{
name|this
operator|.
name|writer
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|sync
parameter_list|()
throws|throws
name|IOException
block|{
name|this
operator|.
name|writer
operator|.
name|sync
argument_list|()
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|writer_out
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|writer_out
operator|.
name|sync
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

