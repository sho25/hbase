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
name|Collections
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
name|client
operator|.
name|TestFromClientSideWithCoprocessor
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
name|coprocessor
operator|.
name|BaseRegionObserver
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
name|coprocessor
operator|.
name|ObserverContext
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
name|coprocessor
operator|.
name|RegionCoprocessorEnvironment
import|;
end_import

begin_comment
comment|/**  * RegionObserver that just reimplements the default behavior,  * in order to validate that all the necessary APIs for this are public  * This observer is also used in {@link TestFromClientSideWithCoprocessor} and  * {@link TestCompactionWithCoprocessor} to make sure that a wide range  * of functionality still behaves as expected.  */
end_comment

begin_class
specifier|public
class|class
name|NoOpScanPolicyObserver
extends|extends
name|BaseRegionObserver
block|{
comment|/**    * Reimplement the default behavior    */
annotation|@
name|Override
specifier|public
name|InternalScanner
name|preFlushScannerOpen
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|c
parameter_list|,
name|Store
name|store
parameter_list|,
name|KeyValueScanner
name|memstoreScanner
parameter_list|,
name|InternalScanner
name|s
parameter_list|)
throws|throws
name|IOException
block|{
name|Store
operator|.
name|ScanInfo
name|oldSI
init|=
name|store
operator|.
name|getScanInfo
argument_list|()
decl_stmt|;
name|Store
operator|.
name|ScanInfo
name|scanInfo
init|=
operator|new
name|Store
operator|.
name|ScanInfo
argument_list|(
name|store
operator|.
name|getFamily
argument_list|()
argument_list|,
name|oldSI
operator|.
name|getTtl
argument_list|()
argument_list|,
name|oldSI
operator|.
name|getTimeToPurgeDeletes
argument_list|()
argument_list|,
name|oldSI
operator|.
name|getComparator
argument_list|()
argument_list|)
decl_stmt|;
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
name|scan
operator|.
name|setMaxVersions
argument_list|(
name|oldSI
operator|.
name|getMaxVersions
argument_list|()
argument_list|)
expr_stmt|;
return|return
operator|new
name|StoreScanner
argument_list|(
name|store
argument_list|,
name|scanInfo
argument_list|,
name|scan
argument_list|,
name|Collections
operator|.
name|singletonList
argument_list|(
name|memstoreScanner
argument_list|)
argument_list|,
name|ScanType
operator|.
name|MINOR_COMPACT
argument_list|,
name|store
operator|.
name|getHRegion
argument_list|()
operator|.
name|getSmallestReadPoint
argument_list|()
argument_list|,
name|HConstants
operator|.
name|OLDEST_TIMESTAMP
argument_list|)
return|;
block|}
comment|/**    * Reimplement the default behavior    */
annotation|@
name|Override
specifier|public
name|InternalScanner
name|preCompactScannerOpen
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|c
parameter_list|,
name|Store
name|store
parameter_list|,
name|List
argument_list|<
name|?
extends|extends
name|KeyValueScanner
argument_list|>
name|scanners
parameter_list|,
name|ScanType
name|scanType
parameter_list|,
name|long
name|earliestPutTs
parameter_list|,
name|InternalScanner
name|s
parameter_list|)
throws|throws
name|IOException
block|{
comment|// this demonstrates how to override the scanners default behavior
name|Store
operator|.
name|ScanInfo
name|oldSI
init|=
name|store
operator|.
name|getScanInfo
argument_list|()
decl_stmt|;
name|Store
operator|.
name|ScanInfo
name|scanInfo
init|=
operator|new
name|Store
operator|.
name|ScanInfo
argument_list|(
name|store
operator|.
name|getFamily
argument_list|()
argument_list|,
name|oldSI
operator|.
name|getTtl
argument_list|()
argument_list|,
name|oldSI
operator|.
name|getTimeToPurgeDeletes
argument_list|()
argument_list|,
name|oldSI
operator|.
name|getComparator
argument_list|()
argument_list|)
decl_stmt|;
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
name|scan
operator|.
name|setMaxVersions
argument_list|(
name|oldSI
operator|.
name|getMaxVersions
argument_list|()
argument_list|)
expr_stmt|;
return|return
operator|new
name|StoreScanner
argument_list|(
name|store
argument_list|,
name|scanInfo
argument_list|,
name|scan
argument_list|,
name|scanners
argument_list|,
name|scanType
argument_list|,
name|store
operator|.
name|getHRegion
argument_list|()
operator|.
name|getSmallestReadPoint
argument_list|()
argument_list|,
name|earliestPutTs
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|KeyValueScanner
name|preStoreScannerOpen
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|c
parameter_list|,
name|Store
name|store
parameter_list|,
specifier|final
name|Scan
name|scan
parameter_list|,
specifier|final
name|NavigableSet
argument_list|<
name|byte
index|[]
argument_list|>
name|targetCols
parameter_list|,
name|KeyValueScanner
name|s
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|StoreScanner
argument_list|(
name|store
argument_list|,
name|store
operator|.
name|getScanInfo
argument_list|()
argument_list|,
name|scan
argument_list|,
name|targetCols
argument_list|)
return|;
block|}
block|}
end_class

end_unit

