begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *<p>  * http://www.apache.org/licenses/LICENSE-2.0  *<p>  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|mapreduce
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
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
name|client
operator|.
name|TableDescriptor
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
name|ImmutableBytesWritable
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
name|nio
operator|.
name|charset
operator|.
name|Charset
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
name|shaded
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|annotations
operator|.
name|VisibleForTesting
import|;
end_import

begin_comment
comment|/**  * Create 3 level tree directory, first level is using table name as parent  * directory and then use family name as child directory, and all related HFiles  * for one family are under child directory  * -tableName1  *     -columnFamilyName1  *     -columnFamilyName2  *         -HFiles  * -tableName2  *     -columnFamilyName1  *         -HFiles  *     -columnFamilyName2  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
annotation|@
name|VisibleForTesting
specifier|public
class|class
name|MultiTableHFileOutputFormat
extends|extends
name|HFileOutputFormat2
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|MultiTableHFileOutputFormat
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/**    * Creates a composite key to use as a mapper output key when using    * MultiTableHFileOutputFormat.configureIncrementaLoad to set up bulk ingest job    *    * @param tableName Name of the Table - Eg: TableName.getNameAsString()    * @param suffix    Usually represents a rowkey when creating a mapper key or column family    * @return          byte[] representation of composite key    */
specifier|public
specifier|static
name|byte
index|[]
name|createCompositeKey
parameter_list|(
name|byte
index|[]
name|tableName
parameter_list|,
name|byte
index|[]
name|suffix
parameter_list|)
block|{
return|return
name|combineTableNameSuffix
argument_list|(
name|tableName
argument_list|,
name|suffix
argument_list|)
return|;
block|}
comment|/**    * Alternate api which accepts an ImmutableBytesWritable for the suffix    * @see MultiTableHFileOutputFormat#createCompositeKey(byte[], byte[])    */
specifier|public
specifier|static
name|byte
index|[]
name|createCompositeKey
parameter_list|(
name|byte
index|[]
name|tableName
parameter_list|,
name|ImmutableBytesWritable
name|suffix
parameter_list|)
block|{
return|return
name|combineTableNameSuffix
argument_list|(
name|tableName
argument_list|,
name|suffix
operator|.
name|get
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * Alternate api which accepts a String for the tableName and ImmutableBytesWritable for the    * suffix    * @see MultiTableHFileOutputFormat#createCompositeKey(byte[], byte[])    */
specifier|public
specifier|static
name|byte
index|[]
name|createCompositeKey
parameter_list|(
name|String
name|tableName
parameter_list|,
name|ImmutableBytesWritable
name|suffix
parameter_list|)
block|{
return|return
name|combineTableNameSuffix
argument_list|(
name|tableName
operator|.
name|getBytes
argument_list|(
name|Charset
operator|.
name|forName
argument_list|(
literal|"UTF-8"
argument_list|)
argument_list|)
argument_list|,
name|suffix
operator|.
name|get
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * Analogous to    * {@link HFileOutputFormat2#configureIncrementalLoad(Job, TableDescriptor, RegionLocator)},    * this function will configure the requisite number of reducers to write HFiles for multple    * tables simultaneously    *    * @param job                   See {@link org.apache.hadoop.mapreduce.Job}    * @param multiTableDescriptors Table descriptor and region locator pairs    * @throws IOException    */
specifier|public
specifier|static
name|void
name|configureIncrementalLoad
parameter_list|(
name|Job
name|job
parameter_list|,
name|List
argument_list|<
name|TableInfo
argument_list|>
name|multiTableDescriptors
parameter_list|)
throws|throws
name|IOException
block|{
name|MultiTableHFileOutputFormat
operator|.
name|configureIncrementalLoad
argument_list|(
name|job
argument_list|,
name|multiTableDescriptors
argument_list|,
name|MultiTableHFileOutputFormat
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
specifier|final
specifier|private
specifier|static
name|int
name|validateCompositeKey
parameter_list|(
name|byte
index|[]
name|keyBytes
parameter_list|)
block|{
name|int
name|separatorIdx
init|=
name|Bytes
operator|.
name|indexOf
argument_list|(
name|keyBytes
argument_list|,
name|tableSeparator
argument_list|)
decl_stmt|;
comment|// Either the separator was not found or a tablename wasn't present or a key wasn't present
if|if
condition|(
name|separatorIdx
operator|==
operator|-
literal|1
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Invalid format for composite key ["
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|keyBytes
argument_list|)
operator|+
literal|"]. Cannot extract tablename and suffix from key"
argument_list|)
throw|;
block|}
return|return
name|separatorIdx
return|;
block|}
specifier|protected
specifier|static
name|byte
index|[]
name|getTableName
parameter_list|(
name|byte
index|[]
name|keyBytes
parameter_list|)
block|{
name|int
name|separatorIdx
init|=
name|validateCompositeKey
argument_list|(
name|keyBytes
argument_list|)
decl_stmt|;
return|return
name|Bytes
operator|.
name|copy
argument_list|(
name|keyBytes
argument_list|,
literal|0
argument_list|,
name|separatorIdx
argument_list|)
return|;
block|}
specifier|protected
specifier|static
name|byte
index|[]
name|getSuffix
parameter_list|(
name|byte
index|[]
name|keyBytes
parameter_list|)
block|{
name|int
name|separatorIdx
init|=
name|validateCompositeKey
argument_list|(
name|keyBytes
argument_list|)
decl_stmt|;
return|return
name|Bytes
operator|.
name|copy
argument_list|(
name|keyBytes
argument_list|,
name|separatorIdx
operator|+
literal|1
argument_list|,
name|keyBytes
operator|.
name|length
operator|-
name|separatorIdx
operator|-
literal|1
argument_list|)
return|;
block|}
block|}
end_class

end_unit
