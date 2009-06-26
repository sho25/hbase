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
name|io
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
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
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
name|io
operator|.
name|hfile
operator|.
name|LruBlockCache
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
name|ClassSize
import|;
end_import

begin_import
import|import
name|junit
operator|.
name|framework
operator|.
name|TestCase
import|;
end_import

begin_comment
comment|/**  * Testing the sizing that HeapSize offers and compares to the size given by  * ClassSize.   */
end_comment

begin_class
specifier|public
class|class
name|TestHeapSize
extends|extends
name|TestCase
block|{
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|TestHeapSize
operator|.
name|class
argument_list|)
decl_stmt|;
comment|// List of classes implementing HeapSize
comment|// BatchOperation, BatchUpdate, BlockIndex, Entry, Entry<K,V>, HStoreKey
comment|// KeyValue, LruBlockCache, LruHashMap<K,V>, Put, HLogKey
comment|/**    * Testing the classes that implements HeapSize and are a part of 0.20.     * Some are not tested here for example BlockIndex which is tested in     * TestHFile since it is a non public class    * @throws IOException     */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|public
name|void
name|testSizes
parameter_list|()
throws|throws
name|IOException
block|{
name|Class
name|cl
init|=
literal|null
decl_stmt|;
name|long
name|expected
init|=
literal|0L
decl_stmt|;
name|long
name|actual
init|=
literal|0L
decl_stmt|;
comment|//KeyValue
name|cl
operator|=
name|KeyValue
operator|.
name|class
expr_stmt|;
name|expected
operator|=
name|ClassSize
operator|.
name|estimateBase
argument_list|(
name|cl
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|KeyValue
name|kv
init|=
operator|new
name|KeyValue
argument_list|()
decl_stmt|;
name|actual
operator|=
name|kv
operator|.
name|heapSize
argument_list|()
expr_stmt|;
if|if
condition|(
name|expected
operator|!=
name|actual
condition|)
block|{
name|ClassSize
operator|.
name|estimateBase
argument_list|(
name|cl
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expected
argument_list|,
name|actual
argument_list|)
expr_stmt|;
block|}
comment|//LruBlockCache
name|cl
operator|=
name|LruBlockCache
operator|.
name|class
expr_stmt|;
name|expected
operator|=
name|ClassSize
operator|.
name|estimateBase
argument_list|(
name|cl
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|LruBlockCache
name|c
init|=
operator|new
name|LruBlockCache
argument_list|(
literal|102400
argument_list|,
literal|1024
argument_list|)
decl_stmt|;
comment|//Since minimum size for the for a LruBlockCache is 1
comment|//we need to remove one reference from the heapsize
name|actual
operator|=
name|c
operator|.
name|heapSize
argument_list|()
expr_stmt|;
comment|// - ClassSize.REFERENCE_SIZE;
if|if
condition|(
name|expected
operator|!=
name|actual
condition|)
block|{
name|ClassSize
operator|.
name|estimateBase
argument_list|(
name|cl
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expected
argument_list|,
name|actual
argument_list|)
expr_stmt|;
block|}
comment|//Put
name|cl
operator|=
name|Put
operator|.
name|class
expr_stmt|;
name|expected
operator|=
name|ClassSize
operator|.
name|estimateBase
argument_list|(
name|cl
argument_list|,
literal|false
argument_list|)
expr_stmt|;
comment|//The actual TreeMap is not included in the above calculation
name|expected
operator|+=
name|ClassSize
operator|.
name|TREEMAP
expr_stmt|;
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
literal|""
argument_list|)
argument_list|)
decl_stmt|;
name|actual
operator|=
name|put
operator|.
name|heapSize
argument_list|()
expr_stmt|;
if|if
condition|(
name|expected
operator|!=
name|actual
condition|)
block|{
name|ClassSize
operator|.
name|estimateBase
argument_list|(
name|cl
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expected
argument_list|,
name|actual
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

