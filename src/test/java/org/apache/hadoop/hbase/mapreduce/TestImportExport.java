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
name|mapreduce
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
name|assertTrue
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
name|HBaseTestingUtility
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
name|MediumTests
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
name|MiniHBaseCluster
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
name|Delete
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
name|client
operator|.
name|HTable
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
name|Result
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
name|ResultScanner
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
name|mapreduce
operator|.
name|Job
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
name|GenericOptionsParser
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|After
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|AfterClass
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Before
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|BeforeClass
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

begin_class
annotation|@
name|Category
argument_list|(
name|MediumTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestImportExport
block|{
specifier|private
specifier|static
specifier|final
name|HBaseTestingUtility
name|UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|ROW1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row1"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|ROW2
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row2"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|FAMILYA_STRING
init|=
literal|"a"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|FAMILYB_STRING
init|=
literal|"b"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|FAMILYA
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|FAMILYA_STRING
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|FAMILYB
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|FAMILYB_STRING
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|QUAL
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"q"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|OUTPUT_DIR
init|=
literal|"outputdir"
decl_stmt|;
specifier|private
specifier|static
name|MiniHBaseCluster
name|cluster
decl_stmt|;
specifier|private
specifier|static
name|long
name|now
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|beforeClass
parameter_list|()
throws|throws
name|Exception
block|{
name|cluster
operator|=
name|UTIL
operator|.
name|startMiniCluster
argument_list|()
expr_stmt|;
name|UTIL
operator|.
name|startMiniMapReduceCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|afterClass
parameter_list|()
throws|throws
name|Exception
block|{
name|UTIL
operator|.
name|shutdownMiniMapReduceCluster
argument_list|()
expr_stmt|;
name|UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Before
annotation|@
name|After
specifier|public
name|void
name|cleanup
parameter_list|()
throws|throws
name|Exception
block|{
name|FileSystem
name|fs
init|=
name|FileSystem
operator|.
name|get
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|fs
operator|.
name|delete
argument_list|(
operator|new
name|Path
argument_list|(
name|OUTPUT_DIR
argument_list|)
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test simple replication case with column mapping    * @throws Exception    */
annotation|@
name|Test
specifier|public
name|void
name|testSimpleCase
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|EXPORT_TABLE
init|=
literal|"exportSimpleCase"
decl_stmt|;
name|HTable
name|t
init|=
name|UTIL
operator|.
name|createTable
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|EXPORT_TABLE
argument_list|)
argument_list|,
name|FAMILYA
argument_list|)
decl_stmt|;
name|Put
name|p
init|=
operator|new
name|Put
argument_list|(
name|ROW1
argument_list|)
decl_stmt|;
name|p
operator|.
name|add
argument_list|(
name|FAMILYA
argument_list|,
name|QUAL
argument_list|,
name|now
argument_list|,
name|QUAL
argument_list|)
expr_stmt|;
name|p
operator|.
name|add
argument_list|(
name|FAMILYA
argument_list|,
name|QUAL
argument_list|,
name|now
operator|+
literal|1
argument_list|,
name|QUAL
argument_list|)
expr_stmt|;
name|p
operator|.
name|add
argument_list|(
name|FAMILYA
argument_list|,
name|QUAL
argument_list|,
name|now
operator|+
literal|2
argument_list|,
name|QUAL
argument_list|)
expr_stmt|;
name|t
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
name|p
operator|=
operator|new
name|Put
argument_list|(
name|ROW2
argument_list|)
expr_stmt|;
name|p
operator|.
name|add
argument_list|(
name|FAMILYA
argument_list|,
name|QUAL
argument_list|,
name|now
argument_list|,
name|QUAL
argument_list|)
expr_stmt|;
name|p
operator|.
name|add
argument_list|(
name|FAMILYA
argument_list|,
name|QUAL
argument_list|,
name|now
operator|+
literal|1
argument_list|,
name|QUAL
argument_list|)
expr_stmt|;
name|p
operator|.
name|add
argument_list|(
name|FAMILYA
argument_list|,
name|QUAL
argument_list|,
name|now
operator|+
literal|2
argument_list|,
name|QUAL
argument_list|)
expr_stmt|;
name|t
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
name|String
index|[]
name|args
init|=
operator|new
name|String
index|[]
block|{
name|EXPORT_TABLE
block|,
name|OUTPUT_DIR
block|,
literal|"1000"
block|}
decl_stmt|;
name|GenericOptionsParser
name|opts
init|=
operator|new
name|GenericOptionsParser
argument_list|(
operator|new
name|Configuration
argument_list|(
name|cluster
operator|.
name|getConfiguration
argument_list|()
argument_list|)
argument_list|,
name|args
argument_list|)
decl_stmt|;
name|Configuration
name|conf
init|=
name|opts
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|args
operator|=
name|opts
operator|.
name|getRemainingArgs
argument_list|()
expr_stmt|;
name|Job
name|job
init|=
name|Export
operator|.
name|createSubmittableJob
argument_list|(
name|conf
argument_list|,
name|args
argument_list|)
decl_stmt|;
name|job
operator|.
name|waitForCompletion
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|job
operator|.
name|isSuccessful
argument_list|()
argument_list|)
expr_stmt|;
name|String
name|IMPORT_TABLE
init|=
literal|"importTableSimpleCase"
decl_stmt|;
name|t
operator|=
name|UTIL
operator|.
name|createTable
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|IMPORT_TABLE
argument_list|)
argument_list|,
name|FAMILYB
argument_list|)
expr_stmt|;
name|args
operator|=
operator|new
name|String
index|[]
block|{
literal|"-D"
operator|+
name|Import
operator|.
name|CF_RENAME_PROP
operator|+
literal|"="
operator|+
name|FAMILYA_STRING
operator|+
literal|":"
operator|+
name|FAMILYB_STRING
block|,
name|IMPORT_TABLE
block|,
name|OUTPUT_DIR
block|}
expr_stmt|;
name|opts
operator|=
operator|new
name|GenericOptionsParser
argument_list|(
operator|new
name|Configuration
argument_list|(
name|cluster
operator|.
name|getConfiguration
argument_list|()
argument_list|)
argument_list|,
name|args
argument_list|)
expr_stmt|;
name|conf
operator|=
name|opts
operator|.
name|getConfiguration
argument_list|()
expr_stmt|;
name|args
operator|=
name|opts
operator|.
name|getRemainingArgs
argument_list|()
expr_stmt|;
name|job
operator|=
name|Import
operator|.
name|createSubmittableJob
argument_list|(
name|conf
argument_list|,
name|args
argument_list|)
expr_stmt|;
name|job
operator|.
name|waitForCompletion
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|job
operator|.
name|isSuccessful
argument_list|()
argument_list|)
expr_stmt|;
name|Get
name|g
init|=
operator|new
name|Get
argument_list|(
name|ROW1
argument_list|)
decl_stmt|;
name|g
operator|.
name|setMaxVersions
argument_list|()
expr_stmt|;
name|Result
name|r
init|=
name|t
operator|.
name|get
argument_list|(
name|g
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|r
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|g
operator|=
operator|new
name|Get
argument_list|(
name|ROW2
argument_list|)
expr_stmt|;
name|g
operator|.
name|setMaxVersions
argument_list|()
expr_stmt|;
name|r
operator|=
name|t
operator|.
name|get
argument_list|(
name|g
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|r
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testWithDeletes
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|EXPORT_TABLE
init|=
literal|"exportWithDeletes"
decl_stmt|;
name|HTableDescriptor
name|desc
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|EXPORT_TABLE
argument_list|)
decl_stmt|;
name|desc
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|FAMILYA
argument_list|,
name|HColumnDescriptor
operator|.
name|DEFAULT_MIN_VERSIONS
argument_list|,
literal|5
argument_list|,
comment|/* versions */
literal|true
comment|/* keep deleted cells */
argument_list|,
name|HColumnDescriptor
operator|.
name|DEFAULT_COMPRESSION
argument_list|,
name|HColumnDescriptor
operator|.
name|DEFAULT_IN_MEMORY
argument_list|,
name|HColumnDescriptor
operator|.
name|DEFAULT_BLOCKCACHE
argument_list|,
name|HColumnDescriptor
operator|.
name|DEFAULT_BLOCKSIZE
argument_list|,
name|HColumnDescriptor
operator|.
name|DEFAULT_TTL
argument_list|,
name|HColumnDescriptor
operator|.
name|DEFAULT_BLOOMFILTER
argument_list|,
name|HConstants
operator|.
name|REPLICATION_SCOPE_LOCAL
argument_list|)
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|getHBaseAdmin
argument_list|()
operator|.
name|createTable
argument_list|(
name|desc
argument_list|)
expr_stmt|;
name|HTable
name|t
init|=
operator|new
name|HTable
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|EXPORT_TABLE
argument_list|)
decl_stmt|;
name|Put
name|p
init|=
operator|new
name|Put
argument_list|(
name|ROW1
argument_list|)
decl_stmt|;
name|p
operator|.
name|add
argument_list|(
name|FAMILYA
argument_list|,
name|QUAL
argument_list|,
name|now
argument_list|,
name|QUAL
argument_list|)
expr_stmt|;
name|p
operator|.
name|add
argument_list|(
name|FAMILYA
argument_list|,
name|QUAL
argument_list|,
name|now
operator|+
literal|1
argument_list|,
name|QUAL
argument_list|)
expr_stmt|;
name|p
operator|.
name|add
argument_list|(
name|FAMILYA
argument_list|,
name|QUAL
argument_list|,
name|now
operator|+
literal|2
argument_list|,
name|QUAL
argument_list|)
expr_stmt|;
name|p
operator|.
name|add
argument_list|(
name|FAMILYA
argument_list|,
name|QUAL
argument_list|,
name|now
operator|+
literal|3
argument_list|,
name|QUAL
argument_list|)
expr_stmt|;
name|p
operator|.
name|add
argument_list|(
name|FAMILYA
argument_list|,
name|QUAL
argument_list|,
name|now
operator|+
literal|4
argument_list|,
name|QUAL
argument_list|)
expr_stmt|;
name|t
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
name|Delete
name|d
init|=
operator|new
name|Delete
argument_list|(
name|ROW1
argument_list|,
name|now
operator|+
literal|3
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|t
operator|.
name|delete
argument_list|(
name|d
argument_list|)
expr_stmt|;
name|d
operator|=
operator|new
name|Delete
argument_list|(
name|ROW1
argument_list|)
expr_stmt|;
name|d
operator|.
name|deleteColumns
argument_list|(
name|FAMILYA
argument_list|,
name|QUAL
argument_list|,
name|now
operator|+
literal|2
argument_list|)
expr_stmt|;
name|t
operator|.
name|delete
argument_list|(
name|d
argument_list|)
expr_stmt|;
name|String
index|[]
name|args
init|=
operator|new
name|String
index|[]
block|{
literal|"-D"
operator|+
name|Export
operator|.
name|RAW_SCAN
operator|+
literal|"=true"
block|,
name|EXPORT_TABLE
block|,
name|OUTPUT_DIR
block|,
literal|"1000"
block|}
decl_stmt|;
name|GenericOptionsParser
name|opts
init|=
operator|new
name|GenericOptionsParser
argument_list|(
operator|new
name|Configuration
argument_list|(
name|cluster
operator|.
name|getConfiguration
argument_list|()
argument_list|)
argument_list|,
name|args
argument_list|)
decl_stmt|;
name|Configuration
name|conf
init|=
name|opts
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|args
operator|=
name|opts
operator|.
name|getRemainingArgs
argument_list|()
expr_stmt|;
name|Job
name|job
init|=
name|Export
operator|.
name|createSubmittableJob
argument_list|(
name|conf
argument_list|,
name|args
argument_list|)
decl_stmt|;
name|job
operator|.
name|waitForCompletion
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|job
operator|.
name|isSuccessful
argument_list|()
argument_list|)
expr_stmt|;
name|String
name|IMPORT_TABLE
init|=
literal|"importWithDeletes"
decl_stmt|;
name|desc
operator|=
operator|new
name|HTableDescriptor
argument_list|(
name|IMPORT_TABLE
argument_list|)
expr_stmt|;
name|desc
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|FAMILYA
argument_list|,
name|HColumnDescriptor
operator|.
name|DEFAULT_MIN_VERSIONS
argument_list|,
literal|5
argument_list|,
comment|/* versions */
literal|true
comment|/* keep deleted cells */
argument_list|,
name|HColumnDescriptor
operator|.
name|DEFAULT_COMPRESSION
argument_list|,
name|HColumnDescriptor
operator|.
name|DEFAULT_IN_MEMORY
argument_list|,
name|HColumnDescriptor
operator|.
name|DEFAULT_BLOCKCACHE
argument_list|,
name|HColumnDescriptor
operator|.
name|DEFAULT_BLOCKSIZE
argument_list|,
name|HColumnDescriptor
operator|.
name|DEFAULT_TTL
argument_list|,
name|HColumnDescriptor
operator|.
name|DEFAULT_BLOOMFILTER
argument_list|,
name|HConstants
operator|.
name|REPLICATION_SCOPE_LOCAL
argument_list|)
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|getHBaseAdmin
argument_list|()
operator|.
name|createTable
argument_list|(
name|desc
argument_list|)
expr_stmt|;
name|t
operator|.
name|close
argument_list|()
expr_stmt|;
name|t
operator|=
operator|new
name|HTable
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|IMPORT_TABLE
argument_list|)
expr_stmt|;
name|args
operator|=
operator|new
name|String
index|[]
block|{
name|IMPORT_TABLE
block|,
name|OUTPUT_DIR
block|}
expr_stmt|;
name|opts
operator|=
operator|new
name|GenericOptionsParser
argument_list|(
operator|new
name|Configuration
argument_list|(
name|cluster
operator|.
name|getConfiguration
argument_list|()
argument_list|)
argument_list|,
name|args
argument_list|)
expr_stmt|;
name|conf
operator|=
name|opts
operator|.
name|getConfiguration
argument_list|()
expr_stmt|;
name|args
operator|=
name|opts
operator|.
name|getRemainingArgs
argument_list|()
expr_stmt|;
name|job
operator|=
name|Import
operator|.
name|createSubmittableJob
argument_list|(
name|conf
argument_list|,
name|args
argument_list|)
expr_stmt|;
name|job
operator|.
name|waitForCompletion
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|job
operator|.
name|isSuccessful
argument_list|()
argument_list|)
expr_stmt|;
name|Scan
name|s
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
name|s
operator|.
name|setMaxVersions
argument_list|()
expr_stmt|;
name|s
operator|.
name|setRaw
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|ResultScanner
name|scanner
init|=
name|t
operator|.
name|getScanner
argument_list|(
name|s
argument_list|)
decl_stmt|;
name|Result
name|r
init|=
name|scanner
operator|.
name|next
argument_list|()
decl_stmt|;
name|KeyValue
index|[]
name|res
init|=
name|r
operator|.
name|raw
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|res
index|[
literal|0
index|]
operator|.
name|isDeleteFamily
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|now
operator|+
literal|4
argument_list|,
name|res
index|[
literal|1
index|]
operator|.
name|getTimestamp
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|now
operator|+
literal|3
argument_list|,
name|res
index|[
literal|2
index|]
operator|.
name|getTimestamp
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|res
index|[
literal|3
index|]
operator|.
name|isDelete
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|now
operator|+
literal|2
argument_list|,
name|res
index|[
literal|4
index|]
operator|.
name|getTimestamp
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|now
operator|+
literal|1
argument_list|,
name|res
index|[
literal|5
index|]
operator|.
name|getTimestamp
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|now
argument_list|,
name|res
index|[
literal|6
index|]
operator|.
name|getTimestamp
argument_list|()
argument_list|)
expr_stmt|;
name|t
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

