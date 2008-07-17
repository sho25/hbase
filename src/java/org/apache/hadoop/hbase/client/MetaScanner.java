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
name|client
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
name|List
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
name|HRegionLocation
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
name|RowResult
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
name|Writables
import|;
end_import

begin_comment
comment|/**  * Scanner class that contains the<code>.META.</code> table scanning logic   * and uses a Retryable scanner. Provided visitors will be called  * for each row.  */
end_comment

begin_class
class|class
name|MetaScanner
implements|implements
name|HConstants
block|{
comment|/**    * Scans the meta table and calls a visitor on each RowResult and uses a empty    * start row value as table name.    *     * @param configuration    * @param visitor A custom visitor    * @throws IOException    */
specifier|public
specifier|static
name|void
name|metaScan
parameter_list|(
name|HBaseConfiguration
name|configuration
parameter_list|,
name|MetaScannerVisitor
name|visitor
parameter_list|)
throws|throws
name|IOException
block|{
name|metaScan
argument_list|(
name|configuration
argument_list|,
name|visitor
argument_list|,
name|EMPTY_START_ROW
argument_list|)
expr_stmt|;
block|}
comment|/**    * Scans the meta table and calls a visitor on each RowResult. Uses a table    * name to locate meta regions.    *     * @param configuration    * @param visitor    * @param tableName    * @throws IOException    */
specifier|public
specifier|static
name|void
name|metaScan
parameter_list|(
name|HBaseConfiguration
name|configuration
parameter_list|,
name|MetaScannerVisitor
name|visitor
parameter_list|,
name|byte
index|[]
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
name|HConnection
name|connection
init|=
name|HConnectionManager
operator|.
name|getConnection
argument_list|(
name|configuration
argument_list|)
decl_stmt|;
name|boolean
name|toContinue
init|=
literal|true
decl_stmt|;
name|byte
index|[]
name|startRow
init|=
name|Bytes
operator|.
name|equals
argument_list|(
name|tableName
argument_list|,
name|EMPTY_START_ROW
argument_list|)
condition|?
name|tableName
else|:
name|HRegionInfo
operator|.
name|createRegionName
argument_list|(
name|tableName
argument_list|,
literal|null
argument_list|,
name|NINES
argument_list|)
decl_stmt|;
comment|// Scan over each meta region
do|do
block|{
name|ScannerCallable
name|callable
init|=
operator|new
name|ScannerCallable
argument_list|(
name|connection
argument_list|,
name|META_TABLE_NAME
argument_list|,
name|COL_REGIONINFO_ARRAY
argument_list|,
name|tableName
argument_list|,
name|LATEST_TIMESTAMP
argument_list|,
literal|null
argument_list|)
decl_stmt|;
try|try
block|{
comment|// Open scanner
name|connection
operator|.
name|getRegionServerWithRetries
argument_list|(
name|callable
argument_list|)
expr_stmt|;
while|while
condition|(
name|toContinue
condition|)
block|{
name|RowResult
name|rowResult
init|=
name|connection
operator|.
name|getRegionServerWithRetries
argument_list|(
name|callable
argument_list|)
decl_stmt|;
if|if
condition|(
name|rowResult
operator|==
literal|null
operator|||
name|rowResult
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
break|break;
block|}
name|HRegionInfo
name|info
init|=
name|Writables
operator|.
name|getHRegionInfo
argument_list|(
name|rowResult
operator|.
name|get
argument_list|(
name|COL_REGIONINFO
argument_list|)
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|byte
index|[]
argument_list|>
name|parse
init|=
name|HRegionInfo
operator|.
name|parseMetaRegionRow
argument_list|(
name|info
operator|.
name|getRegionName
argument_list|()
argument_list|)
decl_stmt|;
name|HRegionLocation
name|regionLocation
init|=
name|connection
operator|.
name|locateRegion
argument_list|(
name|parse
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|parse
operator|.
name|get
argument_list|(
literal|1
argument_list|)
argument_list|)
decl_stmt|;
name|toContinue
operator|=
name|visitor
operator|.
name|processRow
argument_list|(
name|rowResult
argument_list|,
name|regionLocation
argument_list|,
name|info
argument_list|)
expr_stmt|;
block|}
comment|// Advance the startRow to the end key of the current region
name|startRow
operator|=
name|callable
operator|.
name|getHRegionInfo
argument_list|()
operator|.
name|getEndKey
argument_list|()
expr_stmt|;
block|}
finally|finally
block|{
comment|// Close scanner
name|callable
operator|.
name|setClose
argument_list|()
expr_stmt|;
name|connection
operator|.
name|getRegionServerWithRetries
argument_list|(
name|callable
argument_list|)
expr_stmt|;
block|}
block|}
do|while
condition|(
name|Bytes
operator|.
name|compareTo
argument_list|(
name|startRow
argument_list|,
name|LAST_ROW
argument_list|)
operator|!=
literal|0
condition|)
do|;
block|}
comment|/**    * Visitor class called to process each row of the .META. table    */
interface|interface
name|MetaScannerVisitor
block|{
comment|/**      * Visitor method that accepts a RowResult and the meta region location.      * Implementations can return false to stop the region's loop if it becomes      * unnecessary for some reason.      *       * @param rowResult      * @param regionLocation      * @param info      * @return A boolean to know if it should continue to loop in the region      * @throws IOException      */
specifier|public
name|boolean
name|processRow
parameter_list|(
name|RowResult
name|rowResult
parameter_list|,
name|HRegionLocation
name|regionLocation
parameter_list|,
name|HRegionInfo
name|info
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
block|}
end_class

end_unit

