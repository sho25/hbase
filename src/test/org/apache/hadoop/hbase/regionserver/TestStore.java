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
package|;
end_package

begin_import
import|import
name|junit
operator|.
name|framework
operator|.
name|TestCase
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
name|HBaseConfiguration
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
name|HColumnDescriptor
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
name|HRegionInfo
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
name|HTableDescriptor
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
name|hbase
operator|.
name|client
operator|.
name|Get
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
name|io
operator|.
name|hfile
operator|.
name|HFile
operator|.
name|Writer
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
name|util
operator|.
name|Bytes
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
name|util
operator|.
name|Progressable
import|;
end_import

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
name|util
operator|.
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Iterator
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|NavigableSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|TreeSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ConcurrentSkipListSet
import|;
end_import

begin_comment
comment|/**  * Test class fosr the Store   */
end_comment

begin_class
specifier|public
class|class
name|TestStore
extends|extends
name|TestCase
block|{
name|Store
name|store
decl_stmt|;
name|byte
index|[]
name|table
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"table"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|family
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"family"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|row
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|qf1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"qf1"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|qf2
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"qf2"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|qf3
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"qf3"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|qf4
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"qf4"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|qf5
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"qf5"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|qf6
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"qf6"
argument_list|)
decl_stmt|;
name|NavigableSet
argument_list|<
name|byte
index|[]
argument_list|>
name|qualifiers
init|=
operator|new
name|ConcurrentSkipListSet
argument_list|<
name|byte
index|[]
argument_list|>
argument_list|(
name|Bytes
operator|.
name|BYTES_COMPARATOR
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|KeyValue
argument_list|>
name|expected
init|=
operator|new
name|ArrayList
argument_list|<
name|KeyValue
argument_list|>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|KeyValue
argument_list|>
name|result
init|=
operator|new
name|ArrayList
argument_list|<
name|KeyValue
argument_list|>
argument_list|()
decl_stmt|;
name|long
name|id
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|Get
name|get
init|=
operator|new
name|Get
argument_list|(
name|row
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|DIR
init|=
literal|"test/build/data/TestStore/"
decl_stmt|;
comment|/**    * Setup    * @throws IOException    */
annotation|@
name|Override
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|IOException
block|{
name|qualifiers
operator|.
name|add
argument_list|(
name|qf1
argument_list|)
expr_stmt|;
name|qualifiers
operator|.
name|add
argument_list|(
name|qf3
argument_list|)
expr_stmt|;
name|qualifiers
operator|.
name|add
argument_list|(
name|qf5
argument_list|)
expr_stmt|;
name|Iterator
argument_list|<
name|byte
index|[]
argument_list|>
name|iter
init|=
name|qualifiers
operator|.
name|iterator
argument_list|()
decl_stmt|;
while|while
condition|(
name|iter
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|byte
index|[]
name|next
init|=
name|iter
operator|.
name|next
argument_list|()
decl_stmt|;
name|expected
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|row
argument_list|,
name|family
argument_list|,
name|next
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|get
operator|.
name|addColumn
argument_list|(
name|family
argument_list|,
name|next
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|init
parameter_list|(
name|String
name|methodName
parameter_list|)
throws|throws
name|IOException
block|{
comment|//Setting up a Store
name|Path
name|basedir
init|=
operator|new
name|Path
argument_list|(
name|DIR
operator|+
name|methodName
argument_list|)
decl_stmt|;
name|Path
name|logdir
init|=
operator|new
name|Path
argument_list|(
name|DIR
operator|+
name|methodName
operator|+
literal|"/logs"
argument_list|)
decl_stmt|;
name|HColumnDescriptor
name|hcd
init|=
operator|new
name|HColumnDescriptor
argument_list|(
name|family
argument_list|)
decl_stmt|;
name|HBaseConfiguration
name|conf
init|=
operator|new
name|HBaseConfiguration
argument_list|()
decl_stmt|;
name|FileSystem
name|fs
init|=
name|FileSystem
operator|.
name|get
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|Path
name|reconstructionLog
init|=
literal|null
decl_stmt|;
name|Progressable
name|reporter
init|=
literal|null
decl_stmt|;
name|fs
operator|.
name|delete
argument_list|(
name|logdir
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|HTableDescriptor
name|htd
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|table
argument_list|)
decl_stmt|;
name|htd
operator|.
name|addFamily
argument_list|(
name|hcd
argument_list|)
expr_stmt|;
name|HRegionInfo
name|info
init|=
operator|new
name|HRegionInfo
argument_list|(
name|htd
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|HLog
name|hlog
init|=
operator|new
name|HLog
argument_list|(
name|fs
argument_list|,
name|logdir
argument_list|,
name|conf
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|HRegion
name|region
init|=
operator|new
name|HRegion
argument_list|(
name|basedir
argument_list|,
name|hlog
argument_list|,
name|fs
argument_list|,
name|conf
argument_list|,
name|info
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|store
operator|=
operator|new
name|Store
argument_list|(
name|basedir
argument_list|,
name|region
argument_list|,
name|hcd
argument_list|,
name|fs
argument_list|,
name|reconstructionLog
argument_list|,
name|conf
argument_list|,
name|reporter
argument_list|)
expr_stmt|;
block|}
comment|//////////////////////////////////////////////////////////////////////////////
comment|// Get tests
comment|//////////////////////////////////////////////////////////////////////////////
comment|/**    * Test for hbase-1686.    * @throws IOException    */
specifier|public
name|void
name|testEmptyStoreFile
parameter_list|()
throws|throws
name|IOException
block|{
name|init
argument_list|(
name|this
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
comment|// Write a store file.
name|this
operator|.
name|store
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|row
argument_list|,
name|family
argument_list|,
name|qf1
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|store
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|row
argument_list|,
name|family
argument_list|,
name|qf2
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|flush
argument_list|(
literal|1
argument_list|)
expr_stmt|;
comment|// Now put in place an empty store file.  Its a little tricky.  Have to
comment|// do manually with hacked in sequence id.
name|StoreFile
name|f
init|=
name|this
operator|.
name|store
operator|.
name|getStorefiles
argument_list|()
operator|.
name|firstEntry
argument_list|()
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|Path
name|storedir
init|=
name|f
operator|.
name|getPath
argument_list|()
operator|.
name|getParent
argument_list|()
decl_stmt|;
name|long
name|seqid
init|=
name|f
operator|.
name|getMaxSequenceId
argument_list|()
decl_stmt|;
name|HBaseConfiguration
name|c
init|=
operator|new
name|HBaseConfiguration
argument_list|()
decl_stmt|;
name|FileSystem
name|fs
init|=
name|FileSystem
operator|.
name|get
argument_list|(
name|c
argument_list|)
decl_stmt|;
name|Writer
name|w
init|=
name|StoreFile
operator|.
name|getWriter
argument_list|(
name|fs
argument_list|,
name|storedir
argument_list|)
decl_stmt|;
name|StoreFile
operator|.
name|appendMetadata
argument_list|(
name|w
argument_list|,
name|seqid
operator|+
literal|1
argument_list|)
expr_stmt|;
name|w
operator|.
name|close
argument_list|()
expr_stmt|;
name|this
operator|.
name|store
operator|.
name|close
argument_list|()
expr_stmt|;
comment|// Reopen it... should pick up two files
name|this
operator|.
name|store
operator|=
operator|new
name|Store
argument_list|(
name|storedir
operator|.
name|getParent
argument_list|()
operator|.
name|getParent
argument_list|()
argument_list|,
name|this
operator|.
name|store
operator|.
name|getHRegion
argument_list|()
argument_list|,
name|this
operator|.
name|store
operator|.
name|getFamily
argument_list|()
argument_list|,
name|fs
argument_list|,
literal|null
argument_list|,
name|c
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
name|this
operator|.
name|store
operator|.
name|getHRegionInfo
argument_list|()
operator|.
name|getEncodedName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|this
operator|.
name|store
operator|.
name|getStorefilesCount
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|store
operator|.
name|get
argument_list|(
name|get
argument_list|,
name|qualifiers
argument_list|,
name|result
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|result
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Getting data from memstore only    * @throws IOException    */
specifier|public
name|void
name|testGet_FromMemStoreOnly
parameter_list|()
throws|throws
name|IOException
block|{
name|init
argument_list|(
name|this
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
comment|//Put data in memstore
name|this
operator|.
name|store
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|row
argument_list|,
name|family
argument_list|,
name|qf1
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|store
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|row
argument_list|,
name|family
argument_list|,
name|qf2
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|store
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|row
argument_list|,
name|family
argument_list|,
name|qf3
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|store
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|row
argument_list|,
name|family
argument_list|,
name|qf4
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|store
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|row
argument_list|,
name|family
argument_list|,
name|qf5
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|store
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|row
argument_list|,
name|family
argument_list|,
name|qf6
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
comment|//Get
name|this
operator|.
name|store
operator|.
name|get
argument_list|(
name|get
argument_list|,
name|qualifiers
argument_list|,
name|result
argument_list|)
expr_stmt|;
comment|//Compare
name|assertCheck
argument_list|()
expr_stmt|;
block|}
comment|/**    * Getting data from files only    * @throws IOException    */
specifier|public
name|void
name|testGet_FromFilesOnly
parameter_list|()
throws|throws
name|IOException
block|{
name|init
argument_list|(
name|this
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
comment|//Put data in memstore
name|this
operator|.
name|store
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|row
argument_list|,
name|family
argument_list|,
name|qf1
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|store
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|row
argument_list|,
name|family
argument_list|,
name|qf2
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
comment|//flush
name|flush
argument_list|(
literal|1
argument_list|)
expr_stmt|;
comment|//Add more data
name|this
operator|.
name|store
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|row
argument_list|,
name|family
argument_list|,
name|qf3
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|store
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|row
argument_list|,
name|family
argument_list|,
name|qf4
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
comment|//flush
name|flush
argument_list|(
literal|2
argument_list|)
expr_stmt|;
comment|//Add more data
name|this
operator|.
name|store
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|row
argument_list|,
name|family
argument_list|,
name|qf5
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|store
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|row
argument_list|,
name|family
argument_list|,
name|qf6
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
comment|//flush
name|flush
argument_list|(
literal|3
argument_list|)
expr_stmt|;
comment|//Get
name|this
operator|.
name|store
operator|.
name|get
argument_list|(
name|get
argument_list|,
name|qualifiers
argument_list|,
name|result
argument_list|)
expr_stmt|;
comment|//Need to sort the result since multiple files
name|Collections
operator|.
name|sort
argument_list|(
name|result
argument_list|,
name|KeyValue
operator|.
name|COMPARATOR
argument_list|)
expr_stmt|;
comment|//Compare
name|assertCheck
argument_list|()
expr_stmt|;
block|}
comment|/**    * Getting data from memstore and files    * @throws IOException    */
specifier|public
name|void
name|testGet_FromMemStoreAndFiles
parameter_list|()
throws|throws
name|IOException
block|{
name|init
argument_list|(
name|this
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
comment|//Put data in memstore
name|this
operator|.
name|store
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|row
argument_list|,
name|family
argument_list|,
name|qf1
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|store
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|row
argument_list|,
name|family
argument_list|,
name|qf2
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
comment|//flush
name|flush
argument_list|(
literal|1
argument_list|)
expr_stmt|;
comment|//Add more data
name|this
operator|.
name|store
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|row
argument_list|,
name|family
argument_list|,
name|qf3
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|store
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|row
argument_list|,
name|family
argument_list|,
name|qf4
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
comment|//flush
name|flush
argument_list|(
literal|2
argument_list|)
expr_stmt|;
comment|//Add more data
name|this
operator|.
name|store
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|row
argument_list|,
name|family
argument_list|,
name|qf5
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|store
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|row
argument_list|,
name|family
argument_list|,
name|qf6
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
comment|//Get
name|this
operator|.
name|store
operator|.
name|get
argument_list|(
name|get
argument_list|,
name|qualifiers
argument_list|,
name|result
argument_list|)
expr_stmt|;
comment|//Need to sort the result since multiple files
name|Collections
operator|.
name|sort
argument_list|(
name|result
argument_list|,
name|KeyValue
operator|.
name|COMPARATOR
argument_list|)
expr_stmt|;
comment|//Compare
name|assertCheck
argument_list|()
expr_stmt|;
block|}
specifier|private
name|void
name|flush
parameter_list|(
name|int
name|storeFilessize
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|store
operator|.
name|snapshot
argument_list|()
expr_stmt|;
name|this
operator|.
name|store
operator|.
name|flushCache
argument_list|(
name|id
operator|++
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|storeFilessize
argument_list|,
name|this
operator|.
name|store
operator|.
name|getStorefiles
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|this
operator|.
name|store
operator|.
name|memstore
operator|.
name|kvset
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|assertCheck
parameter_list|()
block|{
name|assertEquals
argument_list|(
name|expected
operator|.
name|size
argument_list|()
argument_list|,
name|result
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|expected
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|assertEquals
argument_list|(
name|expected
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|result
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|//////////////////////////////////////////////////////////////////////////////
comment|// IncrementColumnValue tests
comment|//////////////////////////////////////////////////////////////////////////////
comment|/*    * test the internal details of how ICV works, especially during a flush scenario.    */
specifier|public
name|void
name|testIncrementColumnValue_ICVDuringFlush
parameter_list|()
throws|throws
name|IOException
block|{
name|init
argument_list|(
name|this
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|long
name|oldValue
init|=
literal|1L
decl_stmt|;
name|long
name|newValue
init|=
literal|3L
decl_stmt|;
name|this
operator|.
name|store
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|row
argument_list|,
name|family
argument_list|,
name|qf1
argument_list|,
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|oldValue
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
comment|// snapshot the store.
name|this
operator|.
name|store
operator|.
name|snapshot
argument_list|()
expr_stmt|;
comment|// add other things:
name|this
operator|.
name|store
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|row
argument_list|,
name|family
argument_list|,
name|qf2
argument_list|,
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|oldValue
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
comment|// update during the snapshot.
name|long
name|ret
init|=
name|this
operator|.
name|store
operator|.
name|updateColumnValue
argument_list|(
name|row
argument_list|,
name|family
argument_list|,
name|qf1
argument_list|,
name|newValue
argument_list|)
decl_stmt|;
comment|// memstore should have grown by some amount.
name|assertTrue
argument_list|(
name|ret
operator|>
literal|0
argument_list|)
expr_stmt|;
comment|// then flush.
name|this
operator|.
name|store
operator|.
name|flushCache
argument_list|(
name|id
operator|++
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|this
operator|.
name|store
operator|.
name|getStorefiles
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
comment|// from the one we inserted up there, and a new one
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|this
operator|.
name|store
operator|.
name|memstore
operator|.
name|kvset
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
comment|// how many key/values for this row are there?
name|Get
name|get
init|=
operator|new
name|Get
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|get
operator|.
name|addColumn
argument_list|(
name|family
argument_list|,
name|qf1
argument_list|)
expr_stmt|;
name|get
operator|.
name|setMaxVersions
argument_list|()
expr_stmt|;
comment|// all versions.
name|List
argument_list|<
name|KeyValue
argument_list|>
name|results
init|=
operator|new
name|ArrayList
argument_list|<
name|KeyValue
argument_list|>
argument_list|()
decl_stmt|;
name|NavigableSet
argument_list|<
name|byte
index|[]
argument_list|>
name|cols
init|=
operator|new
name|TreeSet
argument_list|<
name|byte
index|[]
argument_list|>
argument_list|()
decl_stmt|;
name|cols
operator|.
name|add
argument_list|(
name|qf1
argument_list|)
expr_stmt|;
name|this
operator|.
name|store
operator|.
name|get
argument_list|(
name|get
argument_list|,
name|cols
argument_list|,
name|results
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|results
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|long
name|ts1
init|=
name|results
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getTimestamp
argument_list|()
decl_stmt|;
name|long
name|ts2
init|=
name|results
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|getTimestamp
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|ts1
operator|>
name|ts2
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|newValue
argument_list|,
name|Bytes
operator|.
name|toLong
argument_list|(
name|results
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|oldValue
argument_list|,
name|Bytes
operator|.
name|toLong
argument_list|(
name|results
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|getValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

