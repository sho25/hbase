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
name|Set
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
name|regionserver
operator|.
name|DeleteCompare
operator|.
name|DeleteCode
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
name|junit
operator|.
name|framework
operator|.
name|TestCase
import|;
end_import

begin_class
specifier|public
class|class
name|TestDeleteCompare
extends|extends
name|TestCase
block|{
comment|//Cases to compare:
comment|//1. DeleteFamily and whatever of the same row
comment|//2. DeleteColumn and whatever of the same row + qualifier
comment|//3. Delete and the matching put
comment|//4. Big test that include starting on the wrong row and qualifier
specifier|public
name|void
name|testDeleteCompare_DeleteFamily
parameter_list|()
block|{
comment|//Creating memstore
name|Set
argument_list|<
name|KeyValue
argument_list|>
name|memstore
init|=
operator|new
name|TreeSet
argument_list|<
name|KeyValue
argument_list|>
argument_list|(
name|KeyValue
operator|.
name|COMPARATOR
argument_list|)
decl_stmt|;
name|memstore
operator|.
name|add
argument_list|(
name|KeyValueTestUtil
operator|.
name|create
argument_list|(
literal|"row11"
argument_list|,
literal|"fam"
argument_list|,
literal|"col1"
argument_list|,
literal|3
argument_list|,
literal|"d-c"
argument_list|)
argument_list|)
expr_stmt|;
name|memstore
operator|.
name|add
argument_list|(
name|KeyValueTestUtil
operator|.
name|create
argument_list|(
literal|"row11"
argument_list|,
literal|"fam"
argument_list|,
literal|"col1"
argument_list|,
literal|2
argument_list|,
literal|"d-c"
argument_list|)
argument_list|)
expr_stmt|;
name|memstore
operator|.
name|add
argument_list|(
name|KeyValueTestUtil
operator|.
name|create
argument_list|(
literal|"row11"
argument_list|,
literal|"fam"
argument_list|,
literal|"col1"
argument_list|,
literal|1
argument_list|,
literal|"d-c"
argument_list|)
argument_list|)
expr_stmt|;
name|memstore
operator|.
name|add
argument_list|(
name|KeyValueTestUtil
operator|.
name|create
argument_list|(
literal|"row11"
argument_list|,
literal|"fam"
argument_list|,
literal|"col2"
argument_list|,
literal|1
argument_list|,
literal|"d-c"
argument_list|)
argument_list|)
expr_stmt|;
name|memstore
operator|.
name|add
argument_list|(
name|KeyValueTestUtil
operator|.
name|create
argument_list|(
literal|"row11"
argument_list|,
literal|"fam"
argument_list|,
literal|"col3"
argument_list|,
literal|3
argument_list|,
literal|"d-c"
argument_list|)
argument_list|)
expr_stmt|;
name|memstore
operator|.
name|add
argument_list|(
name|KeyValueTestUtil
operator|.
name|create
argument_list|(
literal|"row11"
argument_list|,
literal|"fam"
argument_list|,
literal|"col3"
argument_list|,
literal|2
argument_list|,
literal|"d-c"
argument_list|)
argument_list|)
expr_stmt|;
name|memstore
operator|.
name|add
argument_list|(
name|KeyValueTestUtil
operator|.
name|create
argument_list|(
literal|"row11"
argument_list|,
literal|"fam"
argument_list|,
literal|"col3"
argument_list|,
literal|1
argument_list|,
literal|"d-c"
argument_list|)
argument_list|)
expr_stmt|;
name|memstore
operator|.
name|add
argument_list|(
name|KeyValueTestUtil
operator|.
name|create
argument_list|(
literal|"row21"
argument_list|,
literal|"fam"
argument_list|,
literal|"col1"
argument_list|,
literal|1
argument_list|,
literal|"d-c"
argument_list|)
argument_list|)
expr_stmt|;
comment|//Creating expected result
name|List
argument_list|<
name|DeleteCode
argument_list|>
name|expected
init|=
operator|new
name|ArrayList
argument_list|<
name|DeleteCode
argument_list|>
argument_list|()
decl_stmt|;
name|expected
operator|.
name|add
argument_list|(
name|DeleteCode
operator|.
name|SKIP
argument_list|)
expr_stmt|;
name|expected
operator|.
name|add
argument_list|(
name|DeleteCode
operator|.
name|DELETE
argument_list|)
expr_stmt|;
name|expected
operator|.
name|add
argument_list|(
name|DeleteCode
operator|.
name|DELETE
argument_list|)
expr_stmt|;
name|expected
operator|.
name|add
argument_list|(
name|DeleteCode
operator|.
name|DELETE
argument_list|)
expr_stmt|;
name|expected
operator|.
name|add
argument_list|(
name|DeleteCode
operator|.
name|SKIP
argument_list|)
expr_stmt|;
name|expected
operator|.
name|add
argument_list|(
name|DeleteCode
operator|.
name|DELETE
argument_list|)
expr_stmt|;
name|expected
operator|.
name|add
argument_list|(
name|DeleteCode
operator|.
name|DELETE
argument_list|)
expr_stmt|;
name|expected
operator|.
name|add
argument_list|(
name|DeleteCode
operator|.
name|DONE
argument_list|)
expr_stmt|;
name|KeyValue
name|delete
init|=
name|KeyValueTestUtil
operator|.
name|create
argument_list|(
literal|"row11"
argument_list|,
literal|"fam"
argument_list|,
literal|""
argument_list|,
literal|2
argument_list|,
name|KeyValue
operator|.
name|Type
operator|.
name|DeleteFamily
argument_list|,
literal|"dont-care"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|deleteBuffer
init|=
name|delete
operator|.
name|getBuffer
argument_list|()
decl_stmt|;
name|int
name|deleteRowOffset
init|=
name|delete
operator|.
name|getRowOffset
argument_list|()
decl_stmt|;
name|short
name|deleteRowLen
init|=
name|delete
operator|.
name|getRowLength
argument_list|()
decl_stmt|;
name|int
name|deleteQualifierOffset
init|=
name|delete
operator|.
name|getQualifierOffset
argument_list|()
decl_stmt|;
name|int
name|deleteQualifierLen
init|=
name|delete
operator|.
name|getQualifierLength
argument_list|()
decl_stmt|;
name|int
name|deleteTimestampOffset
init|=
name|deleteQualifierOffset
operator|+
name|deleteQualifierLen
decl_stmt|;
name|byte
name|deleteType
init|=
name|deleteBuffer
index|[
name|deleteTimestampOffset
operator|+
name|Bytes
operator|.
name|SIZEOF_LONG
index|]
decl_stmt|;
name|List
argument_list|<
name|DeleteCode
argument_list|>
name|actual
init|=
operator|new
name|ArrayList
argument_list|<
name|DeleteCode
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|KeyValue
name|mem
range|:
name|memstore
control|)
block|{
name|actual
operator|.
name|add
argument_list|(
name|DeleteCompare
operator|.
name|deleteCompare
argument_list|(
name|mem
argument_list|,
name|deleteBuffer
argument_list|,
name|deleteRowOffset
argument_list|,
name|deleteRowLen
argument_list|,
name|deleteQualifierOffset
argument_list|,
name|deleteQualifierLen
argument_list|,
name|deleteTimestampOffset
argument_list|,
name|deleteType
argument_list|,
name|KeyValue
operator|.
name|KEY_COMPARATOR
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|assertEquals
argument_list|(
name|expected
operator|.
name|size
argument_list|()
argument_list|,
name|actual
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
name|actual
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|testDeleteCompare_DeleteColumn
parameter_list|()
block|{
comment|//Creating memstore
name|Set
argument_list|<
name|KeyValue
argument_list|>
name|memstore
init|=
operator|new
name|TreeSet
argument_list|<
name|KeyValue
argument_list|>
argument_list|(
name|KeyValue
operator|.
name|COMPARATOR
argument_list|)
decl_stmt|;
name|memstore
operator|.
name|add
argument_list|(
name|KeyValueTestUtil
operator|.
name|create
argument_list|(
literal|"row11"
argument_list|,
literal|"fam"
argument_list|,
literal|"col1"
argument_list|,
literal|3
argument_list|,
literal|"d-c"
argument_list|)
argument_list|)
expr_stmt|;
name|memstore
operator|.
name|add
argument_list|(
name|KeyValueTestUtil
operator|.
name|create
argument_list|(
literal|"row11"
argument_list|,
literal|"fam"
argument_list|,
literal|"col1"
argument_list|,
literal|2
argument_list|,
literal|"d-c"
argument_list|)
argument_list|)
expr_stmt|;
name|memstore
operator|.
name|add
argument_list|(
name|KeyValueTestUtil
operator|.
name|create
argument_list|(
literal|"row11"
argument_list|,
literal|"fam"
argument_list|,
literal|"col1"
argument_list|,
literal|1
argument_list|,
literal|"d-c"
argument_list|)
argument_list|)
expr_stmt|;
name|memstore
operator|.
name|add
argument_list|(
name|KeyValueTestUtil
operator|.
name|create
argument_list|(
literal|"row21"
argument_list|,
literal|"fam"
argument_list|,
literal|"col1"
argument_list|,
literal|1
argument_list|,
literal|"d-c"
argument_list|)
argument_list|)
expr_stmt|;
comment|//Creating expected result
name|List
argument_list|<
name|DeleteCode
argument_list|>
name|expected
init|=
operator|new
name|ArrayList
argument_list|<
name|DeleteCode
argument_list|>
argument_list|()
decl_stmt|;
name|expected
operator|.
name|add
argument_list|(
name|DeleteCode
operator|.
name|SKIP
argument_list|)
expr_stmt|;
name|expected
operator|.
name|add
argument_list|(
name|DeleteCode
operator|.
name|DELETE
argument_list|)
expr_stmt|;
name|expected
operator|.
name|add
argument_list|(
name|DeleteCode
operator|.
name|DELETE
argument_list|)
expr_stmt|;
name|expected
operator|.
name|add
argument_list|(
name|DeleteCode
operator|.
name|DONE
argument_list|)
expr_stmt|;
name|KeyValue
name|delete
init|=
name|KeyValueTestUtil
operator|.
name|create
argument_list|(
literal|"row11"
argument_list|,
literal|"fam"
argument_list|,
literal|"col1"
argument_list|,
literal|2
argument_list|,
name|KeyValue
operator|.
name|Type
operator|.
name|DeleteColumn
argument_list|,
literal|"dont-care"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|deleteBuffer
init|=
name|delete
operator|.
name|getBuffer
argument_list|()
decl_stmt|;
name|int
name|deleteRowOffset
init|=
name|delete
operator|.
name|getRowOffset
argument_list|()
decl_stmt|;
name|short
name|deleteRowLen
init|=
name|delete
operator|.
name|getRowLength
argument_list|()
decl_stmt|;
name|int
name|deleteQualifierOffset
init|=
name|delete
operator|.
name|getQualifierOffset
argument_list|()
decl_stmt|;
name|int
name|deleteQualifierLen
init|=
name|delete
operator|.
name|getQualifierLength
argument_list|()
decl_stmt|;
name|int
name|deleteTimestampOffset
init|=
name|deleteQualifierOffset
operator|+
name|deleteQualifierLen
decl_stmt|;
name|byte
name|deleteType
init|=
name|deleteBuffer
index|[
name|deleteTimestampOffset
operator|+
name|Bytes
operator|.
name|SIZEOF_LONG
index|]
decl_stmt|;
name|List
argument_list|<
name|DeleteCode
argument_list|>
name|actual
init|=
operator|new
name|ArrayList
argument_list|<
name|DeleteCode
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|KeyValue
name|mem
range|:
name|memstore
control|)
block|{
name|actual
operator|.
name|add
argument_list|(
name|DeleteCompare
operator|.
name|deleteCompare
argument_list|(
name|mem
argument_list|,
name|deleteBuffer
argument_list|,
name|deleteRowOffset
argument_list|,
name|deleteRowLen
argument_list|,
name|deleteQualifierOffset
argument_list|,
name|deleteQualifierLen
argument_list|,
name|deleteTimestampOffset
argument_list|,
name|deleteType
argument_list|,
name|KeyValue
operator|.
name|KEY_COMPARATOR
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|assertEquals
argument_list|(
name|expected
operator|.
name|size
argument_list|()
argument_list|,
name|actual
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
name|actual
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|testDeleteCompare_Delete
parameter_list|()
block|{
comment|//Creating memstore
name|Set
argument_list|<
name|KeyValue
argument_list|>
name|memstore
init|=
operator|new
name|TreeSet
argument_list|<
name|KeyValue
argument_list|>
argument_list|(
name|KeyValue
operator|.
name|COMPARATOR
argument_list|)
decl_stmt|;
name|memstore
operator|.
name|add
argument_list|(
name|KeyValueTestUtil
operator|.
name|create
argument_list|(
literal|"row11"
argument_list|,
literal|"fam"
argument_list|,
literal|"col1"
argument_list|,
literal|3
argument_list|,
literal|"d-c"
argument_list|)
argument_list|)
expr_stmt|;
name|memstore
operator|.
name|add
argument_list|(
name|KeyValueTestUtil
operator|.
name|create
argument_list|(
literal|"row11"
argument_list|,
literal|"fam"
argument_list|,
literal|"col1"
argument_list|,
literal|2
argument_list|,
literal|"d-c"
argument_list|)
argument_list|)
expr_stmt|;
name|memstore
operator|.
name|add
argument_list|(
name|KeyValueTestUtil
operator|.
name|create
argument_list|(
literal|"row11"
argument_list|,
literal|"fam"
argument_list|,
literal|"col1"
argument_list|,
literal|1
argument_list|,
literal|"d-c"
argument_list|)
argument_list|)
expr_stmt|;
comment|//Creating expected result
name|List
argument_list|<
name|DeleteCode
argument_list|>
name|expected
init|=
operator|new
name|ArrayList
argument_list|<
name|DeleteCode
argument_list|>
argument_list|()
decl_stmt|;
name|expected
operator|.
name|add
argument_list|(
name|DeleteCode
operator|.
name|SKIP
argument_list|)
expr_stmt|;
name|expected
operator|.
name|add
argument_list|(
name|DeleteCode
operator|.
name|DELETE
argument_list|)
expr_stmt|;
name|expected
operator|.
name|add
argument_list|(
name|DeleteCode
operator|.
name|DONE
argument_list|)
expr_stmt|;
name|KeyValue
name|delete
init|=
name|KeyValueTestUtil
operator|.
name|create
argument_list|(
literal|"row11"
argument_list|,
literal|"fam"
argument_list|,
literal|"col1"
argument_list|,
literal|2
argument_list|,
name|KeyValue
operator|.
name|Type
operator|.
name|Delete
argument_list|,
literal|"dont-care"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|deleteBuffer
init|=
name|delete
operator|.
name|getBuffer
argument_list|()
decl_stmt|;
name|int
name|deleteRowOffset
init|=
name|delete
operator|.
name|getRowOffset
argument_list|()
decl_stmt|;
name|short
name|deleteRowLen
init|=
name|delete
operator|.
name|getRowLength
argument_list|()
decl_stmt|;
name|int
name|deleteQualifierOffset
init|=
name|delete
operator|.
name|getQualifierOffset
argument_list|()
decl_stmt|;
name|int
name|deleteQualifierLen
init|=
name|delete
operator|.
name|getQualifierLength
argument_list|()
decl_stmt|;
name|int
name|deleteTimestampOffset
init|=
name|deleteQualifierOffset
operator|+
name|deleteQualifierLen
decl_stmt|;
name|byte
name|deleteType
init|=
name|deleteBuffer
index|[
name|deleteTimestampOffset
operator|+
name|Bytes
operator|.
name|SIZEOF_LONG
index|]
decl_stmt|;
name|List
argument_list|<
name|DeleteCode
argument_list|>
name|actual
init|=
operator|new
name|ArrayList
argument_list|<
name|DeleteCode
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|KeyValue
name|mem
range|:
name|memstore
control|)
block|{
name|actual
operator|.
name|add
argument_list|(
name|DeleteCompare
operator|.
name|deleteCompare
argument_list|(
name|mem
argument_list|,
name|deleteBuffer
argument_list|,
name|deleteRowOffset
argument_list|,
name|deleteRowLen
argument_list|,
name|deleteQualifierOffset
argument_list|,
name|deleteQualifierLen
argument_list|,
name|deleteTimestampOffset
argument_list|,
name|deleteType
argument_list|,
name|KeyValue
operator|.
name|KEY_COMPARATOR
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|assertEquals
argument_list|(
name|expected
operator|.
name|size
argument_list|()
argument_list|,
name|actual
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
name|actual
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|testDeleteCompare_Multiple
parameter_list|()
block|{
comment|//Creating memstore
name|Set
argument_list|<
name|KeyValue
argument_list|>
name|memstore
init|=
operator|new
name|TreeSet
argument_list|<
name|KeyValue
argument_list|>
argument_list|(
name|KeyValue
operator|.
name|COMPARATOR
argument_list|)
decl_stmt|;
name|memstore
operator|.
name|add
argument_list|(
name|KeyValueTestUtil
operator|.
name|create
argument_list|(
literal|"row11"
argument_list|,
literal|"fam"
argument_list|,
literal|"col1"
argument_list|,
literal|1
argument_list|,
literal|"d-c"
argument_list|)
argument_list|)
expr_stmt|;
name|memstore
operator|.
name|add
argument_list|(
name|KeyValueTestUtil
operator|.
name|create
argument_list|(
literal|"row21"
argument_list|,
literal|"fam"
argument_list|,
literal|"col1"
argument_list|,
literal|4
argument_list|,
literal|"d-c"
argument_list|)
argument_list|)
expr_stmt|;
name|memstore
operator|.
name|add
argument_list|(
name|KeyValueTestUtil
operator|.
name|create
argument_list|(
literal|"row21"
argument_list|,
literal|"fam"
argument_list|,
literal|"col1"
argument_list|,
literal|3
argument_list|,
literal|"d-c"
argument_list|)
argument_list|)
expr_stmt|;
name|memstore
operator|.
name|add
argument_list|(
name|KeyValueTestUtil
operator|.
name|create
argument_list|(
literal|"row21"
argument_list|,
literal|"fam"
argument_list|,
literal|"col1"
argument_list|,
literal|2
argument_list|,
literal|"d-c"
argument_list|)
argument_list|)
expr_stmt|;
name|memstore
operator|.
name|add
argument_list|(
name|KeyValueTestUtil
operator|.
name|create
argument_list|(
literal|"row21"
argument_list|,
literal|"fam"
argument_list|,
literal|"col1"
argument_list|,
literal|1
argument_list|,
name|KeyValue
operator|.
name|Type
operator|.
name|Delete
argument_list|,
literal|"dont-care"
argument_list|)
argument_list|)
expr_stmt|;
name|memstore
operator|.
name|add
argument_list|(
name|KeyValueTestUtil
operator|.
name|create
argument_list|(
literal|"row31"
argument_list|,
literal|"fam"
argument_list|,
literal|"col1"
argument_list|,
literal|1
argument_list|,
literal|"dont-care"
argument_list|)
argument_list|)
expr_stmt|;
comment|//Creating expected result
name|List
argument_list|<
name|DeleteCode
argument_list|>
name|expected
init|=
operator|new
name|ArrayList
argument_list|<
name|DeleteCode
argument_list|>
argument_list|()
decl_stmt|;
name|expected
operator|.
name|add
argument_list|(
name|DeleteCode
operator|.
name|SKIP
argument_list|)
expr_stmt|;
name|expected
operator|.
name|add
argument_list|(
name|DeleteCode
operator|.
name|DELETE
argument_list|)
expr_stmt|;
name|expected
operator|.
name|add
argument_list|(
name|DeleteCode
operator|.
name|DELETE
argument_list|)
expr_stmt|;
name|expected
operator|.
name|add
argument_list|(
name|DeleteCode
operator|.
name|DELETE
argument_list|)
expr_stmt|;
name|expected
operator|.
name|add
argument_list|(
name|DeleteCode
operator|.
name|DELETE
argument_list|)
expr_stmt|;
name|expected
operator|.
name|add
argument_list|(
name|DeleteCode
operator|.
name|DONE
argument_list|)
expr_stmt|;
name|KeyValue
name|delete
init|=
name|KeyValueTestUtil
operator|.
name|create
argument_list|(
literal|"row21"
argument_list|,
literal|"fam"
argument_list|,
literal|"col1"
argument_list|,
literal|5
argument_list|,
name|KeyValue
operator|.
name|Type
operator|.
name|DeleteColumn
argument_list|,
literal|"dont-care"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|deleteBuffer
init|=
name|delete
operator|.
name|getBuffer
argument_list|()
decl_stmt|;
name|int
name|deleteRowOffset
init|=
name|delete
operator|.
name|getRowOffset
argument_list|()
decl_stmt|;
name|short
name|deleteRowLen
init|=
name|delete
operator|.
name|getRowLength
argument_list|()
decl_stmt|;
name|int
name|deleteQualifierOffset
init|=
name|delete
operator|.
name|getQualifierOffset
argument_list|()
decl_stmt|;
name|int
name|deleteQualifierLen
init|=
name|delete
operator|.
name|getQualifierLength
argument_list|()
decl_stmt|;
name|int
name|deleteTimestampOffset
init|=
name|deleteQualifierOffset
operator|+
name|deleteQualifierLen
decl_stmt|;
name|byte
name|deleteType
init|=
name|deleteBuffer
index|[
name|deleteTimestampOffset
operator|+
name|Bytes
operator|.
name|SIZEOF_LONG
index|]
decl_stmt|;
name|List
argument_list|<
name|DeleteCode
argument_list|>
name|actual
init|=
operator|new
name|ArrayList
argument_list|<
name|DeleteCode
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|KeyValue
name|mem
range|:
name|memstore
control|)
block|{
name|actual
operator|.
name|add
argument_list|(
name|DeleteCompare
operator|.
name|deleteCompare
argument_list|(
name|mem
argument_list|,
name|deleteBuffer
argument_list|,
name|deleteRowOffset
argument_list|,
name|deleteRowLen
argument_list|,
name|deleteQualifierOffset
argument_list|,
name|deleteQualifierLen
argument_list|,
name|deleteTimestampOffset
argument_list|,
name|deleteType
argument_list|,
name|KeyValue
operator|.
name|KEY_COMPARATOR
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|assertEquals
argument_list|(
name|expected
operator|.
name|size
argument_list|()
argument_list|,
name|actual
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
name|actual
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

