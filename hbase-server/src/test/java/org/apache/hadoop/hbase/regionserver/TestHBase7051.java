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
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertEquals
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
name|List
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
name|CountDownLatch
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
name|HConstants
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
name|SmallTests
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
name|MultithreadedTestUtil
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
name|MultithreadedTestUtil
operator|.
name|TestContext
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
name|MultithreadedTestUtil
operator|.
name|TestThread
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
name|Mutation
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
name|Put
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
name|Scan
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
name|filter
operator|.
name|BinaryComparator
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
name|filter
operator|.
name|CompareFilter
operator|.
name|CompareOp
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
name|HeapSize
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
name|hbase
operator|.
name|util
operator|.
name|Pair
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|experimental
operator|.
name|categories
operator|.
name|Category
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Lists
import|;
end_import

begin_comment
comment|/**  * Test of HBASE-7051; that checkAndPuts and puts behave atomically with respect to each other.  * Rather than perform a bunch of trials to verify atomicity, this test recreates a race condition  * that causes the test to fail if checkAndPut doesn't wait for outstanding put transactions  * to complete.  It does this by invasively overriding HRegion function to affect the timing of  * the operations.  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
name|SmallTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestHBase7051
block|{
specifier|private
specifier|static
name|CountDownLatch
name|latch
init|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
decl_stmt|;
specifier|private
enum|enum
name|TestStep
block|{
name|INIT
block|,
comment|// initial put of 10 to set value of the cell
name|PUT_STARTED
block|,
comment|// began doing a put of 50 to cell
name|PUT_COMPLETED
block|,
comment|// put complete (released RowLock, but may not have advanced MVCC).
name|CHECKANDPUT_STARTED
block|,
comment|// began checkAndPut: if 10 -> 11
name|CHECKANDPUT_COMPLETED
comment|// completed checkAndPut
comment|// NOTE: at the end of these steps, the value of the cell should be 50, not 11!
block|}
specifier|private
specifier|static
specifier|volatile
name|TestStep
name|testStep
init|=
name|TestStep
operator|.
name|INIT
decl_stmt|;
specifier|private
specifier|final
name|String
name|family
init|=
literal|"f1"
decl_stmt|;
annotation|@
name|Test
specifier|public
name|void
name|testPutAndCheckAndPutInParallel
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|String
name|tableName
init|=
literal|"testPutAndCheckAndPut"
decl_stmt|;
name|Configuration
name|conf
init|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
decl_stmt|;
name|conf
operator|.
name|setClass
argument_list|(
name|HConstants
operator|.
name|REGION_IMPL
argument_list|,
name|MockHRegion
operator|.
name|class
argument_list|,
name|HeapSize
operator|.
name|class
argument_list|)
expr_stmt|;
specifier|final
name|MockHRegion
name|region
init|=
operator|(
name|MockHRegion
operator|)
name|TestHRegion
operator|.
name|initHRegion
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|tableName
argument_list|)
argument_list|,
name|tableName
argument_list|,
name|conf
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|family
argument_list|)
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Pair
argument_list|<
name|Mutation
argument_list|,
name|Integer
argument_list|>
argument_list|>
name|putsAndLocks
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
name|Put
index|[]
name|puts
init|=
operator|new
name|Put
index|[
literal|1
index|]
decl_stmt|;
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"r1"
argument_list|)
argument_list|)
decl_stmt|;
name|put
operator|.
name|add
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|family
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"q1"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"10"
argument_list|)
argument_list|)
expr_stmt|;
name|puts
index|[
literal|0
index|]
operator|=
name|put
expr_stmt|;
name|Pair
argument_list|<
name|Mutation
argument_list|,
name|Integer
argument_list|>
name|pair
init|=
operator|new
name|Pair
argument_list|<
name|Mutation
argument_list|,
name|Integer
argument_list|>
argument_list|(
name|puts
index|[
literal|0
index|]
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|putsAndLocks
operator|.
name|add
argument_list|(
name|pair
argument_list|)
expr_stmt|;
name|region
operator|.
name|batchMutate
argument_list|(
name|putsAndLocks
operator|.
name|toArray
argument_list|(
operator|new
name|Pair
index|[
literal|0
index|]
argument_list|)
argument_list|)
expr_stmt|;
name|MultithreadedTestUtil
operator|.
name|TestContext
name|ctx
init|=
operator|new
name|MultithreadedTestUtil
operator|.
name|TestContext
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|ctx
operator|.
name|addThread
argument_list|(
operator|new
name|PutThread
argument_list|(
name|ctx
argument_list|,
name|region
argument_list|)
argument_list|)
expr_stmt|;
name|ctx
operator|.
name|addThread
argument_list|(
operator|new
name|CheckAndPutThread
argument_list|(
name|ctx
argument_list|,
name|region
argument_list|)
argument_list|)
expr_stmt|;
name|ctx
operator|.
name|startThreads
argument_list|()
expr_stmt|;
while|while
condition|(
name|testStep
operator|!=
name|TestStep
operator|.
name|CHECKANDPUT_COMPLETED
condition|)
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|100
argument_list|)
expr_stmt|;
block|}
name|ctx
operator|.
name|stop
argument_list|()
expr_stmt|;
name|Scan
name|s
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
name|RegionScanner
name|scanner
init|=
name|region
operator|.
name|getScanner
argument_list|(
name|s
argument_list|)
decl_stmt|;
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
name|scanner
operator|.
name|next
argument_list|(
name|results
argument_list|,
literal|2
argument_list|)
expr_stmt|;
for|for
control|(
name|KeyValue
name|keyValue
range|:
name|results
control|)
block|{
name|assertEquals
argument_list|(
literal|"50"
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|keyValue
operator|.
name|getValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
class|class
name|PutThread
extends|extends
name|TestThread
block|{
specifier|private
name|MockHRegion
name|region
decl_stmt|;
name|PutThread
parameter_list|(
name|TestContext
name|ctx
parameter_list|,
name|MockHRegion
name|region
parameter_list|)
block|{
name|super
argument_list|(
name|ctx
argument_list|)
expr_stmt|;
name|this
operator|.
name|region
operator|=
name|region
expr_stmt|;
block|}
specifier|public
name|void
name|doWork
parameter_list|()
throws|throws
name|Exception
block|{
name|List
argument_list|<
name|Pair
argument_list|<
name|Mutation
argument_list|,
name|Integer
argument_list|>
argument_list|>
name|putsAndLocks
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
name|Put
index|[]
name|puts
init|=
operator|new
name|Put
index|[
literal|1
index|]
decl_stmt|;
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"r1"
argument_list|)
argument_list|)
decl_stmt|;
name|put
operator|.
name|add
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|family
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"q1"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"50"
argument_list|)
argument_list|)
expr_stmt|;
name|puts
index|[
literal|0
index|]
operator|=
name|put
expr_stmt|;
name|Pair
argument_list|<
name|Mutation
argument_list|,
name|Integer
argument_list|>
name|pair
init|=
operator|new
name|Pair
argument_list|<
name|Mutation
argument_list|,
name|Integer
argument_list|>
argument_list|(
name|puts
index|[
literal|0
index|]
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|putsAndLocks
operator|.
name|add
argument_list|(
name|pair
argument_list|)
expr_stmt|;
name|testStep
operator|=
name|TestStep
operator|.
name|PUT_STARTED
expr_stmt|;
name|region
operator|.
name|batchMutate
argument_list|(
name|putsAndLocks
operator|.
name|toArray
argument_list|(
operator|new
name|Pair
index|[
literal|0
index|]
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
class|class
name|CheckAndPutThread
extends|extends
name|TestThread
block|{
specifier|private
name|MockHRegion
name|region
decl_stmt|;
name|CheckAndPutThread
parameter_list|(
name|TestContext
name|ctx
parameter_list|,
name|MockHRegion
name|region
parameter_list|)
block|{
name|super
argument_list|(
name|ctx
argument_list|)
expr_stmt|;
name|this
operator|.
name|region
operator|=
name|region
expr_stmt|;
block|}
specifier|public
name|void
name|doWork
parameter_list|()
throws|throws
name|Exception
block|{
name|Put
index|[]
name|puts
init|=
operator|new
name|Put
index|[
literal|1
index|]
decl_stmt|;
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"r1"
argument_list|)
argument_list|)
decl_stmt|;
name|put
operator|.
name|add
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|family
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"q1"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"11"
argument_list|)
argument_list|)
expr_stmt|;
name|puts
index|[
literal|0
index|]
operator|=
name|put
expr_stmt|;
while|while
condition|(
name|testStep
operator|!=
name|TestStep
operator|.
name|PUT_COMPLETED
condition|)
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|100
argument_list|)
expr_stmt|;
block|}
name|testStep
operator|=
name|TestStep
operator|.
name|CHECKANDPUT_STARTED
expr_stmt|;
name|region
operator|.
name|checkAndMutate
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"r1"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|family
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"q1"
argument_list|)
argument_list|,
name|CompareOp
operator|.
name|EQUAL
argument_list|,
operator|new
name|BinaryComparator
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"10"
argument_list|)
argument_list|)
argument_list|,
name|put
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|testStep
operator|=
name|TestStep
operator|.
name|CHECKANDPUT_COMPLETED
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
class|class
name|MockHRegion
extends|extends
name|HRegion
block|{
specifier|public
name|MockHRegion
parameter_list|(
name|Path
name|tableDir
parameter_list|,
name|HLog
name|log
parameter_list|,
name|FileSystem
name|fs
parameter_list|,
name|Configuration
name|conf
parameter_list|,
specifier|final
name|HRegionInfo
name|regionInfo
parameter_list|,
specifier|final
name|HTableDescriptor
name|htd
parameter_list|,
name|RegionServerServices
name|rsServices
parameter_list|)
block|{
name|super
argument_list|(
name|tableDir
argument_list|,
name|log
argument_list|,
name|fs
argument_list|,
name|conf
argument_list|,
name|regionInfo
argument_list|,
name|htd
argument_list|,
name|rsServices
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|releaseRowLock
parameter_list|(
name|Integer
name|lockId
parameter_list|)
block|{
if|if
condition|(
name|testStep
operator|==
name|TestStep
operator|.
name|INIT
condition|)
block|{
name|super
operator|.
name|releaseRowLock
argument_list|(
name|lockId
argument_list|)
expr_stmt|;
return|return;
block|}
if|if
condition|(
name|testStep
operator|==
name|TestStep
operator|.
name|PUT_STARTED
condition|)
block|{
try|try
block|{
name|testStep
operator|=
name|TestStep
operator|.
name|PUT_COMPLETED
expr_stmt|;
name|super
operator|.
name|releaseRowLock
argument_list|(
name|lockId
argument_list|)
expr_stmt|;
comment|// put has been written to the memstore and the row lock has been released, but the
comment|// MVCC has not been advanced.  Prior to fixing HBASE-7051, the following order of
comment|// operations would cause the non-atomicity to show up:
comment|// 1) Put releases row lock (where we are now)
comment|// 2) CheckAndPut grabs row lock and reads the value prior to the put (10)
comment|//    because the MVCC has not advanced
comment|// 3) Put advances MVCC
comment|// So, in order to recreate this order, we wait for the checkAndPut to grab the rowLock
comment|// (see below), and then wait some more to give the checkAndPut time to read the old
comment|// value.
name|latch
operator|.
name|await
argument_list|()
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|1000
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|interrupt
argument_list|()
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|testStep
operator|==
name|TestStep
operator|.
name|CHECKANDPUT_STARTED
condition|)
block|{
name|super
operator|.
name|releaseRowLock
argument_list|(
name|lockId
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|Integer
name|getLock
parameter_list|(
name|Integer
name|lockid
parameter_list|,
name|byte
index|[]
name|row
parameter_list|,
name|boolean
name|waitForLock
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|testStep
operator|==
name|TestStep
operator|.
name|CHECKANDPUT_STARTED
condition|)
block|{
name|latch
operator|.
name|countDown
argument_list|()
expr_stmt|;
block|}
return|return
name|super
operator|.
name|getLock
argument_list|(
name|lockid
argument_list|,
name|row
argument_list|,
name|waitForLock
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

