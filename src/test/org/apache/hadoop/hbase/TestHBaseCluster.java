begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2007 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
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
name|Iterator
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
name|TreeMap
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
name|Text
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
name|HBaseAdmin
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
name|io
operator|.
name|BatchUpdate
import|;
end_import

begin_comment
comment|/**  * Test HBase Master and Region servers, client API   */
end_comment

begin_class
specifier|public
class|class
name|TestHBaseCluster
extends|extends
name|HBaseClusterTestCase
block|{
specifier|private
name|HTableDescriptor
name|desc
decl_stmt|;
specifier|private
name|HBaseAdmin
name|admin
decl_stmt|;
specifier|private
name|HTable
name|table
decl_stmt|;
comment|/** constructor */
specifier|public
name|TestHBaseCluster
parameter_list|()
block|{
name|super
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|this
operator|.
name|desc
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|admin
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|table
operator|=
literal|null
expr_stmt|;
comment|// Make the thread wake frequency a little slower so other threads
comment|// can run
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.server.thread.wakefrequency"
argument_list|,
literal|2000
argument_list|)
expr_stmt|;
comment|// Make lease timeout longer, lease checks less frequent
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.master.lease.period"
argument_list|,
literal|10
operator|*
literal|1000
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.master.lease.thread.wakefrequency"
argument_list|,
literal|5
operator|*
literal|1000
argument_list|)
expr_stmt|;
comment|// Increase the amount of time between client retries
name|conf
operator|.
name|setLong
argument_list|(
literal|"hbase.client.pause"
argument_list|,
literal|15
operator|*
literal|1000
argument_list|)
expr_stmt|;
block|}
comment|/**    * Since all the "tests" depend on the results of the previous test, they are    * not Junit tests that can stand alone. Consequently we have a single Junit    * test that runs the "sub-tests" as private methods.    * @throws IOException     */
specifier|public
name|void
name|testHBaseCluster
parameter_list|()
throws|throws
name|IOException
block|{
name|setup
argument_list|()
expr_stmt|;
name|basic
argument_list|()
expr_stmt|;
name|scanner
argument_list|()
expr_stmt|;
name|listTables
argument_list|()
expr_stmt|;
name|cleanup
argument_list|()
expr_stmt|;
block|}
specifier|private
specifier|static
specifier|final
name|int
name|FIRST_ROW
init|=
literal|1
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|NUM_VALS
init|=
literal|1000
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Text
name|CONTENTS
init|=
operator|new
name|Text
argument_list|(
literal|"contents:"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Text
name|CONTENTS_BASIC
init|=
operator|new
name|Text
argument_list|(
literal|"contents:basic"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|CONTENTSTR
init|=
literal|"contentstr"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Text
name|ANCHOR
init|=
operator|new
name|Text
argument_list|(
literal|"anchor:"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|ANCHORNUM
init|=
literal|"anchor:anchornum-"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|ANCHORSTR
init|=
literal|"anchorstr"
decl_stmt|;
specifier|private
name|void
name|setup
parameter_list|()
throws|throws
name|IOException
block|{
name|desc
operator|=
operator|new
name|HTableDescriptor
argument_list|(
literal|"test"
argument_list|)
expr_stmt|;
name|desc
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|CONTENTS
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|desc
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|ANCHOR
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|admin
operator|=
operator|new
name|HBaseAdmin
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|admin
operator|.
name|createTable
argument_list|(
name|desc
argument_list|)
expr_stmt|;
name|table
operator|=
operator|new
name|HTable
argument_list|(
name|conf
argument_list|,
name|desc
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// Test basic functionality. Writes to contents:basic and anchor:anchornum-*
specifier|private
name|void
name|basic
parameter_list|()
throws|throws
name|IOException
block|{
name|long
name|startTime
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
comment|// Write out a bunch of values
for|for
control|(
name|int
name|k
init|=
name|FIRST_ROW
init|;
name|k
operator|<=
name|NUM_VALS
condition|;
name|k
operator|++
control|)
block|{
name|BatchUpdate
name|b
init|=
operator|new
name|BatchUpdate
argument_list|(
operator|new
name|Text
argument_list|(
literal|"row_"
operator|+
name|k
argument_list|)
argument_list|)
decl_stmt|;
name|b
operator|.
name|put
argument_list|(
name|CONTENTS_BASIC
argument_list|,
operator|(
name|CONTENTSTR
operator|+
name|k
operator|)
operator|.
name|getBytes
argument_list|(
name|HConstants
operator|.
name|UTF8_ENCODING
argument_list|)
argument_list|)
expr_stmt|;
name|b
operator|.
name|put
argument_list|(
operator|new
name|Text
argument_list|(
name|ANCHORNUM
operator|+
name|k
argument_list|)
argument_list|,
operator|(
name|ANCHORSTR
operator|+
name|k
operator|)
operator|.
name|getBytes
argument_list|(
name|HConstants
operator|.
name|UTF8_ENCODING
argument_list|)
argument_list|)
expr_stmt|;
name|table
operator|.
name|commit
argument_list|(
name|b
argument_list|)
expr_stmt|;
block|}
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Write "
operator|+
name|NUM_VALS
operator|+
literal|" rows. Elapsed time: "
operator|+
operator|(
operator|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|startTime
operator|)
operator|/
literal|1000.0
operator|)
argument_list|)
expr_stmt|;
comment|// Read them back in
name|startTime
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
expr_stmt|;
name|Text
name|collabel
init|=
literal|null
decl_stmt|;
for|for
control|(
name|int
name|k
init|=
name|FIRST_ROW
init|;
name|k
operator|<=
name|NUM_VALS
condition|;
name|k
operator|++
control|)
block|{
name|Text
name|rowlabel
init|=
operator|new
name|Text
argument_list|(
literal|"row_"
operator|+
name|k
argument_list|)
decl_stmt|;
name|byte
name|bodydata
index|[]
init|=
name|table
operator|.
name|get
argument_list|(
name|rowlabel
argument_list|,
name|CONTENTS_BASIC
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
literal|"no data for row "
operator|+
name|rowlabel
operator|+
literal|"/"
operator|+
name|CONTENTS_BASIC
argument_list|,
name|bodydata
argument_list|)
expr_stmt|;
name|String
name|bodystr
init|=
operator|new
name|String
argument_list|(
name|bodydata
argument_list|,
name|HConstants
operator|.
name|UTF8_ENCODING
argument_list|)
decl_stmt|;
name|String
name|teststr
init|=
name|CONTENTSTR
operator|+
name|k
decl_stmt|;
name|assertTrue
argument_list|(
literal|"Incorrect value for key: ("
operator|+
name|rowlabel
operator|+
literal|"/"
operator|+
name|CONTENTS_BASIC
operator|+
literal|"), expected: '"
operator|+
name|teststr
operator|+
literal|"' got: '"
operator|+
name|bodystr
operator|+
literal|"'"
argument_list|,
name|teststr
operator|.
name|compareTo
argument_list|(
name|bodystr
argument_list|)
operator|==
literal|0
argument_list|)
expr_stmt|;
name|collabel
operator|=
operator|new
name|Text
argument_list|(
name|ANCHORNUM
operator|+
name|k
argument_list|)
expr_stmt|;
name|bodydata
operator|=
name|table
operator|.
name|get
argument_list|(
name|rowlabel
argument_list|,
name|collabel
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
literal|"no data for row "
operator|+
name|rowlabel
operator|+
literal|"/"
operator|+
name|collabel
argument_list|,
name|bodydata
argument_list|)
expr_stmt|;
name|bodystr
operator|=
operator|new
name|String
argument_list|(
name|bodydata
argument_list|,
name|HConstants
operator|.
name|UTF8_ENCODING
argument_list|)
expr_stmt|;
name|teststr
operator|=
name|ANCHORSTR
operator|+
name|k
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Incorrect value for key: ("
operator|+
name|rowlabel
operator|+
literal|"/"
operator|+
name|collabel
operator|+
literal|"), expected: '"
operator|+
name|teststr
operator|+
literal|"' got: '"
operator|+
name|bodystr
operator|+
literal|"'"
argument_list|,
name|teststr
operator|.
name|compareTo
argument_list|(
name|bodystr
argument_list|)
operator|==
literal|0
argument_list|)
expr_stmt|;
block|}
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Read "
operator|+
name|NUM_VALS
operator|+
literal|" rows. Elapsed time: "
operator|+
operator|(
operator|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|startTime
operator|)
operator|/
literal|1000.0
operator|)
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|scanner
parameter_list|()
throws|throws
name|IOException
block|{
name|Text
index|[]
name|cols
init|=
operator|new
name|Text
index|[]
block|{
operator|new
name|Text
argument_list|(
name|ANCHORNUM
operator|+
literal|"[0-9]+"
argument_list|)
block|,
operator|new
name|Text
argument_list|(
name|CONTENTS_BASIC
argument_list|)
block|}
decl_stmt|;
name|long
name|startTime
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|HScannerInterface
name|s
init|=
name|table
operator|.
name|obtainScanner
argument_list|(
name|cols
argument_list|,
operator|new
name|Text
argument_list|()
argument_list|)
decl_stmt|;
try|try
block|{
name|int
name|contentsFetched
init|=
literal|0
decl_stmt|;
name|int
name|anchorFetched
init|=
literal|0
decl_stmt|;
name|HStoreKey
name|curKey
init|=
operator|new
name|HStoreKey
argument_list|()
decl_stmt|;
name|TreeMap
argument_list|<
name|Text
argument_list|,
name|byte
index|[]
argument_list|>
name|curVals
init|=
operator|new
name|TreeMap
argument_list|<
name|Text
argument_list|,
name|byte
index|[]
argument_list|>
argument_list|()
decl_stmt|;
name|int
name|k
init|=
literal|0
decl_stmt|;
while|while
condition|(
name|s
operator|.
name|next
argument_list|(
name|curKey
argument_list|,
name|curVals
argument_list|)
condition|)
block|{
for|for
control|(
name|Iterator
argument_list|<
name|Text
argument_list|>
name|it
init|=
name|curVals
operator|.
name|keySet
argument_list|()
operator|.
name|iterator
argument_list|()
init|;
name|it
operator|.
name|hasNext
argument_list|()
condition|;
control|)
block|{
name|Text
name|col
init|=
name|it
operator|.
name|next
argument_list|()
decl_stmt|;
name|byte
name|val
index|[]
init|=
name|curVals
operator|.
name|get
argument_list|(
name|col
argument_list|)
decl_stmt|;
name|String
name|curval
init|=
operator|new
name|String
argument_list|(
name|val
argument_list|,
name|HConstants
operator|.
name|UTF8_ENCODING
argument_list|)
operator|.
name|trim
argument_list|()
decl_stmt|;
if|if
condition|(
name|col
operator|.
name|compareTo
argument_list|(
name|CONTENTS_BASIC
argument_list|)
operator|==
literal|0
condition|)
block|{
name|assertTrue
argument_list|(
literal|"Error at:"
operator|+
name|curKey
operator|.
name|getRow
argument_list|()
operator|+
literal|"/"
operator|+
name|curKey
operator|.
name|getTimestamp
argument_list|()
operator|+
literal|", Value for "
operator|+
name|col
operator|+
literal|" should start with: "
operator|+
name|CONTENTSTR
operator|+
literal|", but was fetched as: "
operator|+
name|curval
argument_list|,
name|curval
operator|.
name|startsWith
argument_list|(
name|CONTENTSTR
argument_list|)
argument_list|)
expr_stmt|;
name|contentsFetched
operator|++
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|col
operator|.
name|toString
argument_list|()
operator|.
name|startsWith
argument_list|(
name|ANCHORNUM
argument_list|)
condition|)
block|{
name|assertTrue
argument_list|(
literal|"Error at:"
operator|+
name|curKey
operator|.
name|getRow
argument_list|()
operator|+
literal|"/"
operator|+
name|curKey
operator|.
name|getTimestamp
argument_list|()
operator|+
literal|", Value for "
operator|+
name|col
operator|+
literal|" should start with: "
operator|+
name|ANCHORSTR
operator|+
literal|", but was fetched as: "
operator|+
name|curval
argument_list|,
name|curval
operator|.
name|startsWith
argument_list|(
name|ANCHORSTR
argument_list|)
argument_list|)
expr_stmt|;
name|anchorFetched
operator|++
expr_stmt|;
block|}
else|else
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
name|col
argument_list|)
expr_stmt|;
block|}
block|}
name|curVals
operator|.
name|clear
argument_list|()
expr_stmt|;
name|k
operator|++
expr_stmt|;
block|}
name|assertEquals
argument_list|(
literal|"Expected "
operator|+
name|NUM_VALS
operator|+
literal|" "
operator|+
name|CONTENTS_BASIC
operator|+
literal|" values, but fetched "
operator|+
name|contentsFetched
argument_list|,
name|NUM_VALS
argument_list|,
name|contentsFetched
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Expected "
operator|+
name|NUM_VALS
operator|+
literal|" "
operator|+
name|ANCHORNUM
operator|+
literal|" values, but fetched "
operator|+
name|anchorFetched
argument_list|,
name|NUM_VALS
argument_list|,
name|anchorFetched
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Scanned "
operator|+
name|NUM_VALS
operator|+
literal|" rows. Elapsed time: "
operator|+
operator|(
operator|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|startTime
operator|)
operator|/
literal|1000.0
operator|)
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|s
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|listTables
parameter_list|()
throws|throws
name|IOException
block|{
name|HTableDescriptor
index|[]
name|tables
init|=
name|admin
operator|.
name|listTables
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|tables
operator|.
name|length
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|desc
operator|.
name|getName
argument_list|()
argument_list|,
name|tables
index|[
literal|0
index|]
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|Set
argument_list|<
name|Text
argument_list|>
name|families
init|=
name|tables
index|[
literal|0
index|]
operator|.
name|families
argument_list|()
operator|.
name|keySet
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|families
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|families
operator|.
name|contains
argument_list|(
operator|new
name|Text
argument_list|(
name|CONTENTS
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|families
operator|.
name|contains
argument_list|(
operator|new
name|Text
argument_list|(
name|ANCHOR
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|cleanup
parameter_list|()
throws|throws
name|IOException
block|{
comment|// Delete the table we created
name|admin
operator|.
name|deleteTable
argument_list|(
name|desc
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

