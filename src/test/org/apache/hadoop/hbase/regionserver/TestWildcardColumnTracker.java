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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|HBaseTestCase
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
name|regionserver
operator|.
name|QueryMatcher
operator|.
name|MatchCode
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

begin_class
specifier|public
class|class
name|TestWildcardColumnTracker
extends|extends
name|HBaseTestCase
implements|implements
name|HConstants
block|{
specifier|private
name|boolean
name|PRINT
init|=
literal|false
decl_stmt|;
specifier|public
name|void
name|testGet_SingleVersion
parameter_list|()
block|{
if|if
condition|(
name|PRINT
condition|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"SingleVersion"
argument_list|)
expr_stmt|;
block|}
name|byte
index|[]
name|col1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"col1"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|col2
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"col2"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|col3
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"col3"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|col4
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"col4"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|col5
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"col5"
argument_list|)
decl_stmt|;
comment|//Create tracker
name|List
argument_list|<
name|MatchCode
argument_list|>
name|expected
init|=
operator|new
name|ArrayList
argument_list|<
name|MatchCode
argument_list|>
argument_list|()
decl_stmt|;
name|expected
operator|.
name|add
argument_list|(
name|MatchCode
operator|.
name|INCLUDE
argument_list|)
expr_stmt|;
name|expected
operator|.
name|add
argument_list|(
name|MatchCode
operator|.
name|INCLUDE
argument_list|)
expr_stmt|;
name|expected
operator|.
name|add
argument_list|(
name|MatchCode
operator|.
name|INCLUDE
argument_list|)
expr_stmt|;
name|expected
operator|.
name|add
argument_list|(
name|MatchCode
operator|.
name|INCLUDE
argument_list|)
expr_stmt|;
name|expected
operator|.
name|add
argument_list|(
name|MatchCode
operator|.
name|INCLUDE
argument_list|)
expr_stmt|;
name|int
name|maxVersions
init|=
literal|1
decl_stmt|;
name|ColumnTracker
name|exp
init|=
operator|new
name|WildcardColumnTracker
argument_list|(
name|maxVersions
argument_list|)
decl_stmt|;
comment|//Create "Scanner"
name|List
argument_list|<
name|byte
index|[]
argument_list|>
name|scanner
init|=
operator|new
name|ArrayList
argument_list|<
name|byte
index|[]
argument_list|>
argument_list|()
decl_stmt|;
name|scanner
operator|.
name|add
argument_list|(
name|col1
argument_list|)
expr_stmt|;
name|scanner
operator|.
name|add
argument_list|(
name|col2
argument_list|)
expr_stmt|;
name|scanner
operator|.
name|add
argument_list|(
name|col3
argument_list|)
expr_stmt|;
name|scanner
operator|.
name|add
argument_list|(
name|col4
argument_list|)
expr_stmt|;
name|scanner
operator|.
name|add
argument_list|(
name|col5
argument_list|)
expr_stmt|;
comment|//Initialize result
name|List
argument_list|<
name|MatchCode
argument_list|>
name|result
init|=
operator|new
name|ArrayList
argument_list|<
name|MatchCode
argument_list|>
argument_list|()
decl_stmt|;
comment|//"Match"
for|for
control|(
name|byte
index|[]
name|col
range|:
name|scanner
control|)
block|{
name|result
operator|.
name|add
argument_list|(
name|exp
operator|.
name|checkColumn
argument_list|(
name|col
argument_list|,
literal|0
argument_list|,
name|col
operator|.
name|length
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
if|if
condition|(
name|PRINT
condition|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Expected "
operator|+
name|expected
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|+
literal|", actual "
operator|+
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
block|}
specifier|public
name|void
name|testGet_MultiVersion
parameter_list|()
block|{
if|if
condition|(
name|PRINT
condition|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"\nMultiVersion"
argument_list|)
expr_stmt|;
block|}
name|byte
index|[]
name|col1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"col1"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|col2
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"col2"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|col3
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"col3"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|col4
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"col4"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|col5
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"col5"
argument_list|)
decl_stmt|;
comment|//Create tracker
name|List
argument_list|<
name|MatchCode
argument_list|>
name|expected
init|=
operator|new
name|ArrayList
argument_list|<
name|MatchCode
argument_list|>
argument_list|()
decl_stmt|;
name|int
name|size
init|=
literal|5
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
name|size
condition|;
name|i
operator|++
control|)
block|{
name|expected
operator|.
name|add
argument_list|(
name|MatchCode
operator|.
name|INCLUDE
argument_list|)
expr_stmt|;
name|expected
operator|.
name|add
argument_list|(
name|MatchCode
operator|.
name|INCLUDE
argument_list|)
expr_stmt|;
name|expected
operator|.
name|add
argument_list|(
name|MatchCode
operator|.
name|SKIP
argument_list|)
expr_stmt|;
block|}
name|int
name|maxVersions
init|=
literal|2
decl_stmt|;
name|ColumnTracker
name|exp
init|=
operator|new
name|WildcardColumnTracker
argument_list|(
name|maxVersions
argument_list|)
decl_stmt|;
comment|//Create "Scanner"
name|List
argument_list|<
name|byte
index|[]
argument_list|>
name|scanner
init|=
operator|new
name|ArrayList
argument_list|<
name|byte
index|[]
argument_list|>
argument_list|()
decl_stmt|;
name|scanner
operator|.
name|add
argument_list|(
name|col1
argument_list|)
expr_stmt|;
name|scanner
operator|.
name|add
argument_list|(
name|col1
argument_list|)
expr_stmt|;
name|scanner
operator|.
name|add
argument_list|(
name|col1
argument_list|)
expr_stmt|;
name|scanner
operator|.
name|add
argument_list|(
name|col2
argument_list|)
expr_stmt|;
name|scanner
operator|.
name|add
argument_list|(
name|col2
argument_list|)
expr_stmt|;
name|scanner
operator|.
name|add
argument_list|(
name|col2
argument_list|)
expr_stmt|;
name|scanner
operator|.
name|add
argument_list|(
name|col3
argument_list|)
expr_stmt|;
name|scanner
operator|.
name|add
argument_list|(
name|col3
argument_list|)
expr_stmt|;
name|scanner
operator|.
name|add
argument_list|(
name|col3
argument_list|)
expr_stmt|;
name|scanner
operator|.
name|add
argument_list|(
name|col4
argument_list|)
expr_stmt|;
name|scanner
operator|.
name|add
argument_list|(
name|col4
argument_list|)
expr_stmt|;
name|scanner
operator|.
name|add
argument_list|(
name|col4
argument_list|)
expr_stmt|;
name|scanner
operator|.
name|add
argument_list|(
name|col5
argument_list|)
expr_stmt|;
name|scanner
operator|.
name|add
argument_list|(
name|col5
argument_list|)
expr_stmt|;
name|scanner
operator|.
name|add
argument_list|(
name|col5
argument_list|)
expr_stmt|;
comment|//Initialize result
name|List
argument_list|<
name|MatchCode
argument_list|>
name|result
init|=
operator|new
name|ArrayList
argument_list|<
name|MatchCode
argument_list|>
argument_list|()
decl_stmt|;
comment|//"Match"
for|for
control|(
name|byte
index|[]
name|col
range|:
name|scanner
control|)
block|{
name|result
operator|.
name|add
argument_list|(
name|exp
operator|.
name|checkColumn
argument_list|(
name|col
argument_list|,
literal|0
argument_list|,
name|col
operator|.
name|length
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
if|if
condition|(
name|PRINT
condition|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Expected "
operator|+
name|expected
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|+
literal|", actual "
operator|+
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
block|}
specifier|public
name|void
name|testUpdate_SameColumns
parameter_list|()
block|{
if|if
condition|(
name|PRINT
condition|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"\nUpdate_SameColumns"
argument_list|)
expr_stmt|;
block|}
name|byte
index|[]
name|col1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"col1"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|col2
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"col2"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|col3
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"col3"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|col4
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"col4"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|col5
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"col5"
argument_list|)
decl_stmt|;
comment|//Create tracker
name|List
argument_list|<
name|MatchCode
argument_list|>
name|expected
init|=
operator|new
name|ArrayList
argument_list|<
name|MatchCode
argument_list|>
argument_list|()
decl_stmt|;
name|int
name|size
init|=
literal|10
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
name|size
condition|;
name|i
operator|++
control|)
block|{
name|expected
operator|.
name|add
argument_list|(
name|MatchCode
operator|.
name|INCLUDE
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|5
condition|;
name|i
operator|++
control|)
block|{
name|expected
operator|.
name|add
argument_list|(
name|MatchCode
operator|.
name|SKIP
argument_list|)
expr_stmt|;
block|}
name|int
name|maxVersions
init|=
literal|2
decl_stmt|;
name|ColumnTracker
name|wild
init|=
operator|new
name|WildcardColumnTracker
argument_list|(
name|maxVersions
argument_list|)
decl_stmt|;
comment|//Create "Scanner"
name|List
argument_list|<
name|byte
index|[]
argument_list|>
name|scanner
init|=
operator|new
name|ArrayList
argument_list|<
name|byte
index|[]
argument_list|>
argument_list|()
decl_stmt|;
name|scanner
operator|.
name|add
argument_list|(
name|col1
argument_list|)
expr_stmt|;
name|scanner
operator|.
name|add
argument_list|(
name|col2
argument_list|)
expr_stmt|;
name|scanner
operator|.
name|add
argument_list|(
name|col3
argument_list|)
expr_stmt|;
name|scanner
operator|.
name|add
argument_list|(
name|col4
argument_list|)
expr_stmt|;
name|scanner
operator|.
name|add
argument_list|(
name|col5
argument_list|)
expr_stmt|;
comment|//Initialize result
name|List
argument_list|<
name|MatchCode
argument_list|>
name|result
init|=
operator|new
name|ArrayList
argument_list|<
name|MatchCode
argument_list|>
argument_list|()
decl_stmt|;
comment|//"Match"
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|3
condition|;
name|i
operator|++
control|)
block|{
for|for
control|(
name|byte
index|[]
name|col
range|:
name|scanner
control|)
block|{
name|result
operator|.
name|add
argument_list|(
name|wild
operator|.
name|checkColumn
argument_list|(
name|col
argument_list|,
literal|0
argument_list|,
name|col
operator|.
name|length
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|wild
operator|.
name|update
argument_list|()
expr_stmt|;
block|}
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
if|if
condition|(
name|PRINT
condition|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Expected "
operator|+
name|expected
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|+
literal|", actual "
operator|+
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
block|}
specifier|public
name|void
name|testUpdate_NewColumns
parameter_list|()
block|{
if|if
condition|(
name|PRINT
condition|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"\nUpdate_NewColumns"
argument_list|)
expr_stmt|;
block|}
name|byte
index|[]
name|col1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"col1"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|col2
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"col2"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|col3
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"col3"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|col4
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"col4"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|col5
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"col5"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|col6
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"col6"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|col7
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"col7"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|col8
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"col8"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|col9
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"col9"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|col0
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"col0"
argument_list|)
decl_stmt|;
comment|//Create tracker
name|List
argument_list|<
name|MatchCode
argument_list|>
name|expected
init|=
operator|new
name|ArrayList
argument_list|<
name|MatchCode
argument_list|>
argument_list|()
decl_stmt|;
name|int
name|size
init|=
literal|10
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
name|size
condition|;
name|i
operator|++
control|)
block|{
name|expected
operator|.
name|add
argument_list|(
name|MatchCode
operator|.
name|INCLUDE
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|5
condition|;
name|i
operator|++
control|)
block|{
name|expected
operator|.
name|add
argument_list|(
name|MatchCode
operator|.
name|SKIP
argument_list|)
expr_stmt|;
block|}
name|int
name|maxVersions
init|=
literal|1
decl_stmt|;
name|ColumnTracker
name|wild
init|=
operator|new
name|WildcardColumnTracker
argument_list|(
name|maxVersions
argument_list|)
decl_stmt|;
comment|//Create "Scanner"
name|List
argument_list|<
name|byte
index|[]
argument_list|>
name|scanner
init|=
operator|new
name|ArrayList
argument_list|<
name|byte
index|[]
argument_list|>
argument_list|()
decl_stmt|;
name|scanner
operator|.
name|add
argument_list|(
name|col0
argument_list|)
expr_stmt|;
name|scanner
operator|.
name|add
argument_list|(
name|col1
argument_list|)
expr_stmt|;
name|scanner
operator|.
name|add
argument_list|(
name|col2
argument_list|)
expr_stmt|;
name|scanner
operator|.
name|add
argument_list|(
name|col3
argument_list|)
expr_stmt|;
name|scanner
operator|.
name|add
argument_list|(
name|col4
argument_list|)
expr_stmt|;
comment|//Initialize result
name|List
argument_list|<
name|MatchCode
argument_list|>
name|result
init|=
operator|new
name|ArrayList
argument_list|<
name|MatchCode
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|byte
index|[]
name|col
range|:
name|scanner
control|)
block|{
name|result
operator|.
name|add
argument_list|(
name|wild
operator|.
name|checkColumn
argument_list|(
name|col
argument_list|,
literal|0
argument_list|,
name|col
operator|.
name|length
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|wild
operator|.
name|update
argument_list|()
expr_stmt|;
comment|//Create "Scanner1"
name|List
argument_list|<
name|byte
index|[]
argument_list|>
name|scanner1
init|=
operator|new
name|ArrayList
argument_list|<
name|byte
index|[]
argument_list|>
argument_list|()
decl_stmt|;
name|scanner1
operator|.
name|add
argument_list|(
name|col5
argument_list|)
expr_stmt|;
name|scanner1
operator|.
name|add
argument_list|(
name|col6
argument_list|)
expr_stmt|;
name|scanner1
operator|.
name|add
argument_list|(
name|col7
argument_list|)
expr_stmt|;
name|scanner1
operator|.
name|add
argument_list|(
name|col8
argument_list|)
expr_stmt|;
name|scanner1
operator|.
name|add
argument_list|(
name|col9
argument_list|)
expr_stmt|;
for|for
control|(
name|byte
index|[]
name|col
range|:
name|scanner1
control|)
block|{
name|result
operator|.
name|add
argument_list|(
name|wild
operator|.
name|checkColumn
argument_list|(
name|col
argument_list|,
literal|0
argument_list|,
name|col
operator|.
name|length
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|wild
operator|.
name|update
argument_list|()
expr_stmt|;
comment|//Scanner again
for|for
control|(
name|byte
index|[]
name|col
range|:
name|scanner
control|)
block|{
name|result
operator|.
name|add
argument_list|(
name|wild
operator|.
name|checkColumn
argument_list|(
name|col
argument_list|,
literal|0
argument_list|,
name|col
operator|.
name|length
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|//"Match"
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
if|if
condition|(
name|PRINT
condition|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Expected "
operator|+
name|expected
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|+
literal|", actual "
operator|+
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
block|}
specifier|public
name|void
name|testUpdate_MixedColumns
parameter_list|()
block|{
if|if
condition|(
name|PRINT
condition|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"\nUpdate_NewColumns"
argument_list|)
expr_stmt|;
block|}
name|byte
index|[]
name|col0
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"col0"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|col1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"col1"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|col2
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"col2"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|col3
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"col3"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|col4
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"col4"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|col5
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"col5"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|col6
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"col6"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|col7
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"col7"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|col8
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"col8"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|col9
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"col9"
argument_list|)
decl_stmt|;
comment|//Create tracker
name|List
argument_list|<
name|MatchCode
argument_list|>
name|expected
init|=
operator|new
name|ArrayList
argument_list|<
name|MatchCode
argument_list|>
argument_list|()
decl_stmt|;
name|int
name|size
init|=
literal|5
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
name|size
condition|;
name|i
operator|++
control|)
block|{
name|expected
operator|.
name|add
argument_list|(
name|MatchCode
operator|.
name|INCLUDE
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|size
condition|;
name|i
operator|++
control|)
block|{
name|expected
operator|.
name|add
argument_list|(
name|MatchCode
operator|.
name|SKIP
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|size
condition|;
name|i
operator|++
control|)
block|{
name|expected
operator|.
name|add
argument_list|(
name|MatchCode
operator|.
name|INCLUDE
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|size
condition|;
name|i
operator|++
control|)
block|{
name|expected
operator|.
name|add
argument_list|(
name|MatchCode
operator|.
name|SKIP
argument_list|)
expr_stmt|;
block|}
name|int
name|maxVersions
init|=
literal|1
decl_stmt|;
name|ColumnTracker
name|wild
init|=
operator|new
name|WildcardColumnTracker
argument_list|(
name|maxVersions
argument_list|)
decl_stmt|;
comment|//Create "Scanner"
name|List
argument_list|<
name|byte
index|[]
argument_list|>
name|scanner
init|=
operator|new
name|ArrayList
argument_list|<
name|byte
index|[]
argument_list|>
argument_list|()
decl_stmt|;
name|scanner
operator|.
name|add
argument_list|(
name|col0
argument_list|)
expr_stmt|;
name|scanner
operator|.
name|add
argument_list|(
name|col2
argument_list|)
expr_stmt|;
name|scanner
operator|.
name|add
argument_list|(
name|col4
argument_list|)
expr_stmt|;
name|scanner
operator|.
name|add
argument_list|(
name|col6
argument_list|)
expr_stmt|;
name|scanner
operator|.
name|add
argument_list|(
name|col8
argument_list|)
expr_stmt|;
comment|//Initialize result
name|List
argument_list|<
name|MatchCode
argument_list|>
name|result
init|=
operator|new
name|ArrayList
argument_list|<
name|MatchCode
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
literal|2
condition|;
name|i
operator|++
control|)
block|{
for|for
control|(
name|byte
index|[]
name|col
range|:
name|scanner
control|)
block|{
name|result
operator|.
name|add
argument_list|(
name|wild
operator|.
name|checkColumn
argument_list|(
name|col
argument_list|,
literal|0
argument_list|,
name|col
operator|.
name|length
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|wild
operator|.
name|update
argument_list|()
expr_stmt|;
block|}
comment|//Create "Scanner1"
name|List
argument_list|<
name|byte
index|[]
argument_list|>
name|scanner1
init|=
operator|new
name|ArrayList
argument_list|<
name|byte
index|[]
argument_list|>
argument_list|()
decl_stmt|;
name|scanner1
operator|.
name|add
argument_list|(
name|col1
argument_list|)
expr_stmt|;
name|scanner1
operator|.
name|add
argument_list|(
name|col3
argument_list|)
expr_stmt|;
name|scanner1
operator|.
name|add
argument_list|(
name|col5
argument_list|)
expr_stmt|;
name|scanner1
operator|.
name|add
argument_list|(
name|col7
argument_list|)
expr_stmt|;
name|scanner1
operator|.
name|add
argument_list|(
name|col9
argument_list|)
expr_stmt|;
for|for
control|(
name|byte
index|[]
name|col
range|:
name|scanner1
control|)
block|{
name|result
operator|.
name|add
argument_list|(
name|wild
operator|.
name|checkColumn
argument_list|(
name|col
argument_list|,
literal|0
argument_list|,
name|col
operator|.
name|length
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|wild
operator|.
name|update
argument_list|()
expr_stmt|;
comment|//Scanner again
for|for
control|(
name|byte
index|[]
name|col
range|:
name|scanner
control|)
block|{
name|result
operator|.
name|add
argument_list|(
name|wild
operator|.
name|checkColumn
argument_list|(
name|col
argument_list|,
literal|0
argument_list|,
name|col
operator|.
name|length
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|//"Match"
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
if|if
condition|(
name|PRINT
condition|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Expected "
operator|+
name|expected
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|+
literal|", actual "
operator|+
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
block|}
comment|// HBASE-1781
specifier|public
name|void
name|testStackOverflow
parameter_list|()
block|{
name|int
name|maxVersions
init|=
literal|1
decl_stmt|;
name|ColumnTracker
name|wild
init|=
operator|new
name|WildcardColumnTracker
argument_list|(
name|maxVersions
argument_list|)
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
literal|100000
condition|;
name|i
operator|+=
literal|2
control|)
block|{
name|byte
index|[]
name|col
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"col"
operator|+
name|i
argument_list|)
decl_stmt|;
name|wild
operator|.
name|checkColumn
argument_list|(
name|col
argument_list|,
literal|0
argument_list|,
name|col
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
name|wild
operator|.
name|update
argument_list|()
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<
literal|100000
condition|;
name|i
operator|+=
literal|2
control|)
block|{
name|byte
index|[]
name|col
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"col"
operator|+
name|i
argument_list|)
decl_stmt|;
name|wild
operator|.
name|checkColumn
argument_list|(
name|col
argument_list|,
literal|0
argument_list|,
name|col
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

