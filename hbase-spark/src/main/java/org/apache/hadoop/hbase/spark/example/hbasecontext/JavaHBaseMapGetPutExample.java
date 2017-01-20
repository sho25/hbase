begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one or more  * contributor license agreements.  See the NOTICE file distributed with  * this work for additional information regarding copyright ownership.  * The ASF licenses this file to You under the Apache License, Version 2.0  * (the "License"); you may not use this file except in compliance with  * the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
operator|.
name|spark
operator|.
name|example
operator|.
name|hbasecontext
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
name|TableName
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
name|BufferedMutator
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
name|Connection
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
name|Table
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
name|spark
operator|.
name|JavaHBaseContext
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
name|spark
operator|.
name|SparkConf
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|spark
operator|.
name|api
operator|.
name|java
operator|.
name|JavaRDD
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|spark
operator|.
name|api
operator|.
name|java
operator|.
name|JavaSparkContext
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|spark
operator|.
name|api
operator|.
name|java
operator|.
name|function
operator|.
name|Function
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|spark
operator|.
name|api
operator|.
name|java
operator|.
name|function
operator|.
name|VoidFunction
import|;
end_import

begin_import
import|import
name|scala
operator|.
name|Tuple2
import|;
end_import

begin_comment
comment|/**  * This is a simple example of using the foreachPartition  * method with a HBase connection  */
end_comment

begin_class
specifier|final
specifier|public
class|class
name|JavaHBaseMapGetPutExample
block|{
specifier|private
name|JavaHBaseMapGetPutExample
parameter_list|()
block|{}
specifier|public
specifier|static
name|void
name|main
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
block|{
if|if
condition|(
name|args
operator|.
name|length
operator|<
literal|1
condition|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"JavaHBaseBulkGetExample {tableName}"
argument_list|)
expr_stmt|;
return|return;
block|}
specifier|final
name|String
name|tableName
init|=
name|args
index|[
literal|0
index|]
decl_stmt|;
name|SparkConf
name|sparkConf
init|=
operator|new
name|SparkConf
argument_list|()
operator|.
name|setAppName
argument_list|(
literal|"JavaHBaseBulkGetExample "
operator|+
name|tableName
argument_list|)
decl_stmt|;
name|JavaSparkContext
name|jsc
init|=
operator|new
name|JavaSparkContext
argument_list|(
name|sparkConf
argument_list|)
decl_stmt|;
try|try
block|{
name|List
argument_list|<
name|byte
index|[]
argument_list|>
name|list
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
literal|5
argument_list|)
decl_stmt|;
name|list
operator|.
name|add
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"1"
argument_list|)
argument_list|)
expr_stmt|;
name|list
operator|.
name|add
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"2"
argument_list|)
argument_list|)
expr_stmt|;
name|list
operator|.
name|add
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"3"
argument_list|)
argument_list|)
expr_stmt|;
name|list
operator|.
name|add
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"4"
argument_list|)
argument_list|)
expr_stmt|;
name|list
operator|.
name|add
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"5"
argument_list|)
argument_list|)
expr_stmt|;
name|JavaRDD
argument_list|<
name|byte
index|[]
argument_list|>
name|rdd
init|=
name|jsc
operator|.
name|parallelize
argument_list|(
name|list
argument_list|)
decl_stmt|;
name|Configuration
name|conf
init|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
decl_stmt|;
name|JavaHBaseContext
name|hbaseContext
init|=
operator|new
name|JavaHBaseContext
argument_list|(
name|jsc
argument_list|,
name|conf
argument_list|)
decl_stmt|;
name|hbaseContext
operator|.
name|foreachPartition
argument_list|(
name|rdd
argument_list|,
operator|new
name|VoidFunction
argument_list|<
name|Tuple2
argument_list|<
name|Iterator
argument_list|<
name|byte
index|[]
argument_list|>
argument_list|,
name|Connection
argument_list|>
argument_list|>
argument_list|()
block|{
specifier|public
name|void
name|call
parameter_list|(
name|Tuple2
argument_list|<
name|Iterator
argument_list|<
name|byte
index|[]
argument_list|>
argument_list|,
name|Connection
argument_list|>
name|t
parameter_list|)
throws|throws
name|Exception
block|{
name|Table
name|table
init|=
name|t
operator|.
name|_2
argument_list|()
operator|.
name|getTable
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|tableName
argument_list|)
argument_list|)
decl_stmt|;
name|BufferedMutator
name|mutator
init|=
name|t
operator|.
name|_2
argument_list|()
operator|.
name|getBufferedMutator
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|tableName
argument_list|)
argument_list|)
decl_stmt|;
while|while
condition|(
name|t
operator|.
name|_1
argument_list|()
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|byte
index|[]
name|b
init|=
name|t
operator|.
name|_1
argument_list|()
operator|.
name|next
argument_list|()
decl_stmt|;
name|Result
name|r
init|=
name|table
operator|.
name|get
argument_list|(
operator|new
name|Get
argument_list|(
name|b
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|r
operator|.
name|getExists
argument_list|()
condition|)
block|{
name|mutator
operator|.
name|mutate
argument_list|(
operator|new
name|Put
argument_list|(
name|b
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|mutator
operator|.
name|flush
argument_list|()
expr_stmt|;
name|mutator
operator|.
name|close
argument_list|()
expr_stmt|;
name|table
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|jsc
operator|.
name|stop
argument_list|()
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
class|class
name|GetFunction
implements|implements
name|Function
argument_list|<
name|byte
index|[]
argument_list|,
name|Get
argument_list|>
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
specifier|public
name|Get
name|call
parameter_list|(
name|byte
index|[]
name|v
parameter_list|)
throws|throws
name|Exception
block|{
return|return
operator|new
name|Get
argument_list|(
name|v
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

