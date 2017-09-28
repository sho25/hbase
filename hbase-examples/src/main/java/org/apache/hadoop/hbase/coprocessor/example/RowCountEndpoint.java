begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|coprocessor
operator|.
name|example
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
name|ArrayList
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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|Cell
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
name|CellUtil
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
name|CoprocessorEnvironment
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
name|coprocessor
operator|.
name|CoprocessorException
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
name|RegionCoprocessor
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
name|example
operator|.
name|generated
operator|.
name|ExampleProtos
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
name|filter
operator|.
name|FirstKeyOnlyFilter
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
name|ipc
operator|.
name|CoprocessorRpcUtils
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
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|RpcCallback
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|RpcController
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|Service
import|;
end_import

begin_comment
comment|/**  * Sample coprocessor endpoint exposing a Service interface for counting rows and key values.  *  *<p>  * For the protocol buffer definition of the RowCountService, see the source file located under  * hbase-examples/src/main/protobuf/Examples.proto.  *</p>  */
end_comment

begin_class
specifier|public
class|class
name|RowCountEndpoint
extends|extends
name|ExampleProtos
operator|.
name|RowCountService
implements|implements
name|RegionCoprocessor
block|{
specifier|private
name|RegionCoprocessorEnvironment
name|env
decl_stmt|;
specifier|public
name|RowCountEndpoint
parameter_list|()
block|{   }
comment|/**    * Just returns a reference to this object, which implements the RowCounterService interface.    */
annotation|@
name|Override
specifier|public
name|Iterable
argument_list|<
name|Service
argument_list|>
name|getServices
parameter_list|()
block|{
return|return
name|Collections
operator|.
name|singleton
argument_list|(
name|this
argument_list|)
return|;
block|}
comment|/**    * Returns a count of the rows in the region where this coprocessor is loaded.    */
annotation|@
name|Override
specifier|public
name|void
name|getRowCount
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|ExampleProtos
operator|.
name|CountRequest
name|request
parameter_list|,
name|RpcCallback
argument_list|<
name|ExampleProtos
operator|.
name|CountResponse
argument_list|>
name|done
parameter_list|)
block|{
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
name|scan
operator|.
name|setFilter
argument_list|(
operator|new
name|FirstKeyOnlyFilter
argument_list|()
argument_list|)
expr_stmt|;
name|ExampleProtos
operator|.
name|CountResponse
name|response
init|=
literal|null
decl_stmt|;
name|InternalScanner
name|scanner
init|=
literal|null
decl_stmt|;
try|try
block|{
name|scanner
operator|=
name|env
operator|.
name|getRegion
argument_list|()
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|Cell
argument_list|>
name|results
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|boolean
name|hasMore
init|=
literal|false
decl_stmt|;
name|byte
index|[]
name|lastRow
init|=
literal|null
decl_stmt|;
name|long
name|count
init|=
literal|0
decl_stmt|;
do|do
block|{
name|hasMore
operator|=
name|scanner
operator|.
name|next
argument_list|(
name|results
argument_list|)
expr_stmt|;
for|for
control|(
name|Cell
name|kv
range|:
name|results
control|)
block|{
name|byte
index|[]
name|currentRow
init|=
name|CellUtil
operator|.
name|cloneRow
argument_list|(
name|kv
argument_list|)
decl_stmt|;
if|if
condition|(
name|lastRow
operator|==
literal|null
operator|||
operator|!
name|Bytes
operator|.
name|equals
argument_list|(
name|lastRow
argument_list|,
name|currentRow
argument_list|)
condition|)
block|{
name|lastRow
operator|=
name|currentRow
expr_stmt|;
name|count
operator|++
expr_stmt|;
block|}
block|}
name|results
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
do|while
condition|(
name|hasMore
condition|)
do|;
name|response
operator|=
name|ExampleProtos
operator|.
name|CountResponse
operator|.
name|newBuilder
argument_list|()
operator|.
name|setCount
argument_list|(
name|count
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
name|CoprocessorRpcUtils
operator|.
name|setControllerException
argument_list|(
name|controller
argument_list|,
name|ioe
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
name|scanner
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|scanner
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ignored
parameter_list|)
block|{}
block|}
block|}
name|done
operator|.
name|run
argument_list|(
name|response
argument_list|)
expr_stmt|;
block|}
comment|/**    * Returns a count of all KeyValues in the region where this coprocessor is loaded.    */
annotation|@
name|Override
specifier|public
name|void
name|getKeyValueCount
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|ExampleProtos
operator|.
name|CountRequest
name|request
parameter_list|,
name|RpcCallback
argument_list|<
name|ExampleProtos
operator|.
name|CountResponse
argument_list|>
name|done
parameter_list|)
block|{
name|ExampleProtos
operator|.
name|CountResponse
name|response
init|=
literal|null
decl_stmt|;
name|InternalScanner
name|scanner
init|=
literal|null
decl_stmt|;
try|try
block|{
name|scanner
operator|=
name|env
operator|.
name|getRegion
argument_list|()
operator|.
name|getScanner
argument_list|(
operator|new
name|Scan
argument_list|()
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|Cell
argument_list|>
name|results
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|boolean
name|hasMore
init|=
literal|false
decl_stmt|;
name|long
name|count
init|=
literal|0
decl_stmt|;
do|do
block|{
name|hasMore
operator|=
name|scanner
operator|.
name|next
argument_list|(
name|results
argument_list|)
expr_stmt|;
for|for
control|(
name|Cell
name|kv
range|:
name|results
control|)
block|{
name|count
operator|++
expr_stmt|;
block|}
name|results
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
do|while
condition|(
name|hasMore
condition|)
do|;
name|response
operator|=
name|ExampleProtos
operator|.
name|CountResponse
operator|.
name|newBuilder
argument_list|()
operator|.
name|setCount
argument_list|(
name|count
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
name|CoprocessorRpcUtils
operator|.
name|setControllerException
argument_list|(
name|controller
argument_list|,
name|ioe
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
name|scanner
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|scanner
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ignored
parameter_list|)
block|{}
block|}
block|}
name|done
operator|.
name|run
argument_list|(
name|response
argument_list|)
expr_stmt|;
block|}
comment|/**    * Stores a reference to the coprocessor environment provided by the    * {@link org.apache.hadoop.hbase.regionserver.RegionCoprocessorHost} from the region where this    * coprocessor is loaded.  Since this is a coprocessor endpoint, it always expects to be loaded    * on a table region, so always expects this to be an instance of    * {@link RegionCoprocessorEnvironment}.    * @param env the environment provided by the coprocessor host    * @throws IOException if the provided environment is not an instance of    * {@code RegionCoprocessorEnvironment}    */
annotation|@
name|Override
specifier|public
name|void
name|start
parameter_list|(
name|CoprocessorEnvironment
name|env
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|env
operator|instanceof
name|RegionCoprocessorEnvironment
condition|)
block|{
name|this
operator|.
name|env
operator|=
operator|(
name|RegionCoprocessorEnvironment
operator|)
name|env
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|CoprocessorException
argument_list|(
literal|"Must be loaded on a table region!"
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|stop
parameter_list|(
name|CoprocessorEnvironment
name|env
parameter_list|)
throws|throws
name|IOException
block|{
comment|// nothing to do
block|}
block|}
end_class

end_unit

