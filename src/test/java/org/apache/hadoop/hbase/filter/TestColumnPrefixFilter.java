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
name|filter
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
name|*
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
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashSet
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
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
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
name|KeyValueTestUtil
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
name|regionserver
operator|.
name|HRegion
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
name|InternalScanner
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
name|junit
operator|.
name|Test
import|;
end_import

begin_class
specifier|public
class|class
name|TestColumnPrefixFilter
block|{
specifier|private
specifier|final
specifier|static
name|HBaseTestingUtility
name|TEST_UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
annotation|@
name|Test
specifier|public
name|void
name|testColumnPrefixFilter
parameter_list|()
throws|throws
name|IOException
block|{
name|String
name|family
init|=
literal|"Family"
decl_stmt|;
name|HTableDescriptor
name|htd
init|=
operator|new
name|HTableDescriptor
argument_list|(
literal|"TestColumnPrefixFilter"
argument_list|)
decl_stmt|;
name|htd
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|family
argument_list|)
argument_list|)
expr_stmt|;
name|HRegionInfo
name|info
init|=
operator|new
name|HRegionInfo
argument_list|(
name|htd
operator|.
name|getName
argument_list|()
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|HRegion
name|region
init|=
name|HRegion
operator|.
name|createHRegion
argument_list|(
name|info
argument_list|,
name|HBaseTestingUtility
operator|.
name|getTestDir
argument_list|()
argument_list|,
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|htd
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|rows
init|=
name|generateRandomWords
argument_list|(
literal|100
argument_list|,
literal|"row"
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|columns
init|=
name|generateRandomWords
argument_list|(
literal|10000
argument_list|,
literal|"column"
argument_list|)
decl_stmt|;
name|long
name|maxTimestamp
init|=
literal|2
decl_stmt|;
name|List
argument_list|<
name|KeyValue
argument_list|>
name|kvList
init|=
operator|new
name|ArrayList
argument_list|<
name|KeyValue
argument_list|>
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|KeyValue
argument_list|>
argument_list|>
name|prefixMap
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|KeyValue
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
name|prefixMap
operator|.
name|put
argument_list|(
literal|"p"
argument_list|,
operator|new
name|ArrayList
argument_list|<
name|KeyValue
argument_list|>
argument_list|()
argument_list|)
expr_stmt|;
name|prefixMap
operator|.
name|put
argument_list|(
literal|"s"
argument_list|,
operator|new
name|ArrayList
argument_list|<
name|KeyValue
argument_list|>
argument_list|()
argument_list|)
expr_stmt|;
name|String
name|valueString
init|=
literal|"ValueString"
decl_stmt|;
for|for
control|(
name|String
name|row
range|:
name|rows
control|)
block|{
name|Put
name|p
init|=
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|row
argument_list|)
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|column
range|:
name|columns
control|)
block|{
for|for
control|(
name|long
name|timestamp
init|=
literal|1
init|;
name|timestamp
operator|<=
name|maxTimestamp
condition|;
name|timestamp
operator|++
control|)
block|{
name|KeyValue
name|kv
init|=
name|KeyValueTestUtil
operator|.
name|create
argument_list|(
name|row
argument_list|,
name|family
argument_list|,
name|column
argument_list|,
name|timestamp
argument_list|,
name|valueString
argument_list|)
decl_stmt|;
name|p
operator|.
name|add
argument_list|(
name|kv
argument_list|)
expr_stmt|;
name|kvList
operator|.
name|add
argument_list|(
name|kv
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|s
range|:
name|prefixMap
operator|.
name|keySet
argument_list|()
control|)
block|{
if|if
condition|(
name|column
operator|.
name|startsWith
argument_list|(
name|s
argument_list|)
condition|)
block|{
name|prefixMap
operator|.
name|get
argument_list|(
name|s
argument_list|)
operator|.
name|add
argument_list|(
name|kv
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
name|region
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
block|}
name|ColumnPrefixFilter
name|filter
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
argument_list|()
expr_stmt|;
for|for
control|(
name|String
name|s
range|:
name|prefixMap
operator|.
name|keySet
argument_list|()
control|)
block|{
name|filter
operator|=
operator|new
name|ColumnPrefixFilter
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|s
argument_list|)
argument_list|)
expr_stmt|;
name|scan
operator|.
name|setFilter
argument_list|(
name|filter
argument_list|)
expr_stmt|;
name|InternalScanner
name|scanner
init|=
name|region
operator|.
name|getScanner
argument_list|(
name|scan
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
while|while
condition|(
name|scanner
operator|.
name|next
argument_list|(
name|results
argument_list|)
condition|)
empty_stmt|;
name|assertEquals
argument_list|(
name|prefixMap
operator|.
name|get
argument_list|(
name|s
argument_list|)
operator|.
name|size
argument_list|()
argument_list|,
name|results
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testColumnPrefixFilterWithFilterList
parameter_list|()
throws|throws
name|IOException
block|{
name|String
name|family
init|=
literal|"Family"
decl_stmt|;
name|HTableDescriptor
name|htd
init|=
operator|new
name|HTableDescriptor
argument_list|(
literal|"TestColumnPrefixFilter"
argument_list|)
decl_stmt|;
name|htd
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|family
argument_list|)
argument_list|)
expr_stmt|;
name|HRegionInfo
name|info
init|=
operator|new
name|HRegionInfo
argument_list|(
name|htd
operator|.
name|getName
argument_list|()
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|HRegion
name|region
init|=
name|HRegion
operator|.
name|createHRegion
argument_list|(
name|info
argument_list|,
name|HBaseTestingUtility
operator|.
name|getTestDir
argument_list|()
argument_list|,
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|htd
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|rows
init|=
name|generateRandomWords
argument_list|(
literal|100
argument_list|,
literal|"row"
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|columns
init|=
name|generateRandomWords
argument_list|(
literal|10000
argument_list|,
literal|"column"
argument_list|)
decl_stmt|;
name|long
name|maxTimestamp
init|=
literal|2
decl_stmt|;
name|List
argument_list|<
name|KeyValue
argument_list|>
name|kvList
init|=
operator|new
name|ArrayList
argument_list|<
name|KeyValue
argument_list|>
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|KeyValue
argument_list|>
argument_list|>
name|prefixMap
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|KeyValue
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
name|prefixMap
operator|.
name|put
argument_list|(
literal|"p"
argument_list|,
operator|new
name|ArrayList
argument_list|<
name|KeyValue
argument_list|>
argument_list|()
argument_list|)
expr_stmt|;
name|prefixMap
operator|.
name|put
argument_list|(
literal|"s"
argument_list|,
operator|new
name|ArrayList
argument_list|<
name|KeyValue
argument_list|>
argument_list|()
argument_list|)
expr_stmt|;
name|String
name|valueString
init|=
literal|"ValueString"
decl_stmt|;
for|for
control|(
name|String
name|row
range|:
name|rows
control|)
block|{
name|Put
name|p
init|=
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|row
argument_list|)
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|column
range|:
name|columns
control|)
block|{
for|for
control|(
name|long
name|timestamp
init|=
literal|1
init|;
name|timestamp
operator|<=
name|maxTimestamp
condition|;
name|timestamp
operator|++
control|)
block|{
name|KeyValue
name|kv
init|=
name|KeyValueTestUtil
operator|.
name|create
argument_list|(
name|row
argument_list|,
name|family
argument_list|,
name|column
argument_list|,
name|timestamp
argument_list|,
name|valueString
argument_list|)
decl_stmt|;
name|p
operator|.
name|add
argument_list|(
name|kv
argument_list|)
expr_stmt|;
name|kvList
operator|.
name|add
argument_list|(
name|kv
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|s
range|:
name|prefixMap
operator|.
name|keySet
argument_list|()
control|)
block|{
if|if
condition|(
name|column
operator|.
name|startsWith
argument_list|(
name|s
argument_list|)
condition|)
block|{
name|prefixMap
operator|.
name|get
argument_list|(
name|s
argument_list|)
operator|.
name|add
argument_list|(
name|kv
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
name|region
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
block|}
name|ColumnPrefixFilter
name|filter
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
argument_list|()
expr_stmt|;
for|for
control|(
name|String
name|s
range|:
name|prefixMap
operator|.
name|keySet
argument_list|()
control|)
block|{
name|filter
operator|=
operator|new
name|ColumnPrefixFilter
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|s
argument_list|)
argument_list|)
expr_stmt|;
comment|//this is how this test differs from the one above
name|FilterList
name|filterList
init|=
operator|new
name|FilterList
argument_list|(
name|FilterList
operator|.
name|Operator
operator|.
name|MUST_PASS_ALL
argument_list|)
decl_stmt|;
name|filterList
operator|.
name|addFilter
argument_list|(
name|filter
argument_list|)
expr_stmt|;
name|scan
operator|.
name|setFilter
argument_list|(
name|filterList
argument_list|)
expr_stmt|;
name|InternalScanner
name|scanner
init|=
name|region
operator|.
name|getScanner
argument_list|(
name|scan
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
while|while
condition|(
name|scanner
operator|.
name|next
argument_list|(
name|results
argument_list|)
condition|)
empty_stmt|;
name|assertEquals
argument_list|(
name|prefixMap
operator|.
name|get
argument_list|(
name|s
argument_list|)
operator|.
name|size
argument_list|()
argument_list|,
name|results
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|List
argument_list|<
name|String
argument_list|>
name|generateRandomWords
parameter_list|(
name|int
name|numberOfWords
parameter_list|,
name|String
name|suffix
parameter_list|)
block|{
name|Set
argument_list|<
name|String
argument_list|>
name|wordSet
init|=
operator|new
name|HashSet
argument_list|<
name|String
argument_list|>
argument_list|()
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
name|numberOfWords
condition|;
name|i
operator|++
control|)
block|{
name|int
name|lengthOfWords
init|=
call|(
name|int
call|)
argument_list|(
name|Math
operator|.
name|random
argument_list|()
operator|*
literal|2
argument_list|)
operator|+
literal|1
decl_stmt|;
name|char
index|[]
name|wordChar
init|=
operator|new
name|char
index|[
name|lengthOfWords
index|]
decl_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|wordChar
operator|.
name|length
condition|;
name|j
operator|++
control|)
block|{
name|wordChar
index|[
name|j
index|]
operator|=
call|(
name|char
call|)
argument_list|(
name|Math
operator|.
name|random
argument_list|()
operator|*
literal|26
operator|+
literal|97
argument_list|)
expr_stmt|;
block|}
name|String
name|word
decl_stmt|;
if|if
condition|(
name|suffix
operator|==
literal|null
condition|)
block|{
name|word
operator|=
operator|new
name|String
argument_list|(
name|wordChar
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|word
operator|=
operator|new
name|String
argument_list|(
name|wordChar
argument_list|)
operator|+
name|suffix
expr_stmt|;
block|}
name|wordSet
operator|.
name|add
argument_list|(
name|word
argument_list|)
expr_stmt|;
block|}
name|List
argument_list|<
name|String
argument_list|>
name|wordList
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|(
name|wordSet
argument_list|)
decl_stmt|;
return|return
name|wordList
return|;
block|}
block|}
end_class

end_unit

