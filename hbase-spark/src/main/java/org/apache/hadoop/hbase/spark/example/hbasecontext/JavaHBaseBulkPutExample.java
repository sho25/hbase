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

begin_comment
comment|/**  * This is a simple example of putting records in HBase  * with the bulkPut function.  */
end_comment

begin_class
specifier|final
specifier|public
class|class
name|JavaHBaseBulkPutExample
block|{
specifier|private
name|JavaHBaseBulkPutExample
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
literal|2
condition|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"JavaHBaseBulkPutExample  "
operator|+
literal|"{tableName} {columnFamily}"
argument_list|)
expr_stmt|;
return|return;
block|}
name|String
name|tableName
init|=
name|args
index|[
literal|0
index|]
decl_stmt|;
name|String
name|columnFamily
init|=
name|args
index|[
literal|1
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
literal|"JavaHBaseBulkPutExample "
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
name|String
argument_list|>
name|list
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|list
operator|.
name|add
argument_list|(
literal|"1,"
operator|+
name|columnFamily
operator|+
literal|",a,1"
argument_list|)
expr_stmt|;
name|list
operator|.
name|add
argument_list|(
literal|"2,"
operator|+
name|columnFamily
operator|+
literal|",a,2"
argument_list|)
expr_stmt|;
name|list
operator|.
name|add
argument_list|(
literal|"3,"
operator|+
name|columnFamily
operator|+
literal|",a,3"
argument_list|)
expr_stmt|;
name|list
operator|.
name|add
argument_list|(
literal|"4,"
operator|+
name|columnFamily
operator|+
literal|",a,4"
argument_list|)
expr_stmt|;
name|list
operator|.
name|add
argument_list|(
literal|"5,"
operator|+
name|columnFamily
operator|+
literal|",a,5"
argument_list|)
expr_stmt|;
name|JavaRDD
argument_list|<
name|String
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
name|bulkPut
argument_list|(
name|rdd
argument_list|,
name|TableName
operator|.
name|valueOf
argument_list|(
name|tableName
argument_list|)
argument_list|,
operator|new
name|PutFunction
argument_list|()
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
name|PutFunction
implements|implements
name|Function
argument_list|<
name|String
argument_list|,
name|Put
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
name|Put
name|call
parameter_list|(
name|String
name|v
parameter_list|)
throws|throws
name|Exception
block|{
name|String
index|[]
name|cells
init|=
name|v
operator|.
name|split
argument_list|(
literal|","
argument_list|)
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
name|cells
index|[
literal|0
index|]
argument_list|)
argument_list|)
decl_stmt|;
name|put
operator|.
name|addColumn
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|cells
index|[
literal|1
index|]
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|cells
index|[
literal|2
index|]
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|cells
index|[
literal|3
index|]
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|put
return|;
block|}
block|}
block|}
end_class

end_unit

