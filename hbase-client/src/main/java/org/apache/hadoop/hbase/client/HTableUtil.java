begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|client
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|classification
operator|.
name|InterfaceAudience
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
name|io
operator|.
name|InterruptedIOException
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

begin_comment
comment|/**  * Utility class for HTable.  *   *  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|HTableUtil
block|{
specifier|private
specifier|static
specifier|final
name|int
name|INITIAL_LIST_SIZE
init|=
literal|250
decl_stmt|;
comment|/**    * Processes a List of Puts and writes them to an HTable instance in RegionServer buckets via the htable.put method.     * This will utilize the writeBuffer, thus the writeBuffer flush frequency may be tuned accordingly via htable.setWriteBufferSize.     *<br><br>    * The benefit of submitting Puts in this manner is to minimize the number of RegionServer RPCs in each flush.    *<br><br>    * Assumption #1:  Regions have been pre-created for the table.  If they haven't, then all of the Puts will go to the same region,     * defeating the purpose of this utility method. See the Apache HBase book for an explanation of how to do this.    *<br>    * Assumption #2:  Row-keys are not monotonically increasing.  See the Apache HBase book for an explanation of this problem.      *<br>    * Assumption #3:  That the input list of Puts is big enough to be useful (in the thousands or more).  The intent of this    * method is to process larger chunks of data.    *<br>    * Assumption #4:  htable.setAutoFlush(false) has been set.  This is a requirement to use the writeBuffer.    *<br><br>    * @param htable HTable instance for target HBase table    * @param puts List of Put instances    * @throws IOException if a remote or network exception occurs    *     */
specifier|public
specifier|static
name|void
name|bucketRsPut
parameter_list|(
name|HTable
name|htable
parameter_list|,
name|List
argument_list|<
name|Put
argument_list|>
name|puts
parameter_list|)
throws|throws
name|IOException
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|Put
argument_list|>
argument_list|>
name|putMap
init|=
name|createRsPutMap
argument_list|(
name|htable
argument_list|,
name|puts
argument_list|)
decl_stmt|;
for|for
control|(
name|List
argument_list|<
name|Put
argument_list|>
name|rsPuts
range|:
name|putMap
operator|.
name|values
argument_list|()
control|)
block|{
name|htable
operator|.
name|put
argument_list|(
name|rsPuts
argument_list|)
expr_stmt|;
block|}
name|htable
operator|.
name|flushCommits
argument_list|()
expr_stmt|;
block|}
comment|/**    * Processes a List of Rows (Put, Delete) and writes them to an HTable instance in RegionServer buckets via the htable.batch method.     *<br><br>    * The benefit of submitting Puts in this manner is to minimize the number of RegionServer RPCs, thus this will    * produce one RPC of Puts per RegionServer.    *<br><br>    * Assumption #1:  Regions have been pre-created for the table.  If they haven't, then all of the Puts will go to the same region,     * defeating the purpose of this utility method. See the Apache HBase book for an explanation of how to do this.    *<br>    * Assumption #2:  Row-keys are not monotonically increasing.  See the Apache HBase book for an explanation of this problem.      *<br>    * Assumption #3:  That the input list of Rows is big enough to be useful (in the thousands or more).  The intent of this    * method is to process larger chunks of data.    *<br><br>    * This method accepts a list of Row objects because the underlying .batch method accepts a list of Row objects.    *<br><br>    * @param htable HTable instance for target HBase table    * @param rows List of Row instances    * @throws IOException if a remote or network exception occurs    */
specifier|public
specifier|static
name|void
name|bucketRsBatch
parameter_list|(
name|HTable
name|htable
parameter_list|,
name|List
argument_list|<
name|Row
argument_list|>
name|rows
parameter_list|)
throws|throws
name|IOException
block|{
try|try
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|Row
argument_list|>
argument_list|>
name|rowMap
init|=
name|createRsRowMap
argument_list|(
name|htable
argument_list|,
name|rows
argument_list|)
decl_stmt|;
for|for
control|(
name|List
argument_list|<
name|Row
argument_list|>
name|rsRows
range|:
name|rowMap
operator|.
name|values
argument_list|()
control|)
block|{
name|htable
operator|.
name|batch
argument_list|(
name|rsRows
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
throw|throw
operator|(
name|InterruptedIOException
operator|)
operator|new
name|InterruptedIOException
argument_list|()
operator|.
name|initCause
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
specifier|private
specifier|static
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|Put
argument_list|>
argument_list|>
name|createRsPutMap
parameter_list|(
name|RegionLocator
name|htable
parameter_list|,
name|List
argument_list|<
name|Put
argument_list|>
name|puts
parameter_list|)
throws|throws
name|IOException
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|Put
argument_list|>
argument_list|>
name|putMap
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|Put
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|Put
name|put
range|:
name|puts
control|)
block|{
name|HRegionLocation
name|rl
init|=
name|htable
operator|.
name|getRegionLocation
argument_list|(
name|put
operator|.
name|getRow
argument_list|()
argument_list|)
decl_stmt|;
name|String
name|hostname
init|=
name|rl
operator|.
name|getHostname
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Put
argument_list|>
name|recs
init|=
name|putMap
operator|.
name|get
argument_list|(
name|hostname
argument_list|)
decl_stmt|;
if|if
condition|(
name|recs
operator|==
literal|null
condition|)
block|{
name|recs
operator|=
operator|new
name|ArrayList
argument_list|<
name|Put
argument_list|>
argument_list|(
name|INITIAL_LIST_SIZE
argument_list|)
expr_stmt|;
name|putMap
operator|.
name|put
argument_list|(
name|hostname
argument_list|,
name|recs
argument_list|)
expr_stmt|;
block|}
name|recs
operator|.
name|add
argument_list|(
name|put
argument_list|)
expr_stmt|;
block|}
return|return
name|putMap
return|;
block|}
specifier|private
specifier|static
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|Row
argument_list|>
argument_list|>
name|createRsRowMap
parameter_list|(
name|RegionLocator
name|htable
parameter_list|,
name|List
argument_list|<
name|Row
argument_list|>
name|rows
parameter_list|)
throws|throws
name|IOException
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|Row
argument_list|>
argument_list|>
name|rowMap
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|Row
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|Row
name|row
range|:
name|rows
control|)
block|{
name|HRegionLocation
name|rl
init|=
name|htable
operator|.
name|getRegionLocation
argument_list|(
name|row
operator|.
name|getRow
argument_list|()
argument_list|)
decl_stmt|;
name|String
name|hostname
init|=
name|rl
operator|.
name|getHostname
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Row
argument_list|>
name|recs
init|=
name|rowMap
operator|.
name|get
argument_list|(
name|hostname
argument_list|)
decl_stmt|;
if|if
condition|(
name|recs
operator|==
literal|null
condition|)
block|{
name|recs
operator|=
operator|new
name|ArrayList
argument_list|<
name|Row
argument_list|>
argument_list|(
name|INITIAL_LIST_SIZE
argument_list|)
expr_stmt|;
name|rowMap
operator|.
name|put
argument_list|(
name|hostname
argument_list|,
name|recs
argument_list|)
expr_stmt|;
block|}
name|recs
operator|.
name|add
argument_list|(
name|row
argument_list|)
expr_stmt|;
block|}
return|return
name|rowMap
return|;
block|}
block|}
end_class

end_unit

