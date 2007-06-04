begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2007 The Apache Software Foundation  *  * Licensed under the Apache License, Version 2.0 (the "License");  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|io
operator|.
name|BytesWritable
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

begin_comment
comment|/**  * HConstants holds a bunch of HBase-related constants  */
end_comment

begin_interface
specifier|public
interface|interface
name|HConstants
block|{
comment|// Configuration parameters
comment|// TODO: URL for hbase master like hdfs URLs with host and port.
comment|// Like jdbc URLs?  URLs could be used to refer to table cells?
comment|// jdbc:mysql://[host][,failoverhost...][:port]/[database]
comment|// jdbc:mysql://[host][,failoverhost...][:port]/[database][?propertyName1][=propertyValue1][&propertyName2][=propertyValue2]...
comment|// Key into HBaseConfiguration for the hbase.master address.
comment|// TODO: Support 'local': i.e. default of all running in single
comment|// process.  Same for regionserver. TODO: Is having HBase homed
comment|// on port 60k OK?
specifier|static
specifier|final
name|String
name|MASTER_ADDRESS
init|=
literal|"hbase.master"
decl_stmt|;
specifier|static
specifier|final
name|String
name|DEFAULT_MASTER_ADDRESS
init|=
literal|"localhost:60000"
decl_stmt|;
comment|// Key for hbase.regionserver address.
specifier|static
specifier|final
name|String
name|REGIONSERVER_ADDRESS
init|=
literal|"hbase.regionserver"
decl_stmt|;
specifier|static
specifier|final
name|String
name|DEFAULT_REGIONSERVER_ADDRESS
init|=
literal|"localhost:60010"
decl_stmt|;
specifier|static
specifier|final
name|String
name|THREAD_WAKE_FREQUENCY
init|=
literal|"hbase.server.thread.wakefrequency"
decl_stmt|;
specifier|static
specifier|final
name|String
name|HREGION_DIR
init|=
literal|"hbase.regiondir"
decl_stmt|;
specifier|static
specifier|final
name|String
name|DEFAULT_HREGION_DIR
init|=
literal|"/hbase"
decl_stmt|;
specifier|static
specifier|final
name|String
name|HREGIONDIR_PREFIX
init|=
literal|"hregion_"
decl_stmt|;
comment|// TODO: Someone may try to name a column family 'log'.  If they
comment|// do, it will clash with the HREGION log dir subdirectory. FIX.
specifier|static
specifier|final
name|String
name|HREGION_LOGDIR_NAME
init|=
literal|"log"
decl_stmt|;
specifier|static
specifier|final
name|long
name|DEFAULT_MAX_FILE_SIZE
init|=
literal|128
operator|*
literal|1024
operator|*
literal|1024
decl_stmt|;
comment|// 128MB
comment|// Always store the location of the root table's HRegion.
comment|// This HRegion is never split.
comment|// region name = table + startkey + regionid. This is the row key.
comment|// each row in the root and meta tables describes exactly 1 region
comment|// Do we ever need to know all the information that we are storing?
comment|// The root tables' name.
specifier|static
specifier|final
name|Text
name|ROOT_TABLE_NAME
init|=
operator|new
name|Text
argument_list|(
literal|"--ROOT--"
argument_list|)
decl_stmt|;
comment|// The META tables' name.
specifier|static
specifier|final
name|Text
name|META_TABLE_NAME
init|=
operator|new
name|Text
argument_list|(
literal|"--META--"
argument_list|)
decl_stmt|;
comment|// Defines for the column names used in both ROOT and META HBase 'meta'
comment|// tables.
specifier|static
specifier|final
name|Text
name|COLUMN_FAMILY
init|=
operator|new
name|Text
argument_list|(
literal|"info:"
argument_list|)
decl_stmt|;
specifier|static
specifier|final
name|Text
name|COL_REGIONINFO
init|=
operator|new
name|Text
argument_list|(
name|COLUMN_FAMILY
operator|+
literal|"regioninfo"
argument_list|)
decl_stmt|;
specifier|static
specifier|final
name|Text
name|COL_SERVER
init|=
operator|new
name|Text
argument_list|(
name|COLUMN_FAMILY
operator|+
literal|"server"
argument_list|)
decl_stmt|;
specifier|static
specifier|final
name|Text
name|COL_STARTCODE
init|=
operator|new
name|Text
argument_list|(
name|COLUMN_FAMILY
operator|+
literal|"serverstartcode"
argument_list|)
decl_stmt|;
comment|// Other constants
specifier|static
specifier|final
name|String
name|UTF8_ENCODING
init|=
literal|"UTF-8"
decl_stmt|;
specifier|static
specifier|final
name|BytesWritable
name|DELETE_BYTES
init|=
operator|new
name|BytesWritable
argument_list|(
literal|"HBASE::DELETEVAL"
operator|.
name|getBytes
argument_list|()
argument_list|)
decl_stmt|;
specifier|static
specifier|final
name|BytesWritable
name|COMPLETE_CACHEFLUSH
init|=
operator|new
name|BytesWritable
argument_list|(
literal|"HBASE::CACHEFLUSH"
operator|.
name|getBytes
argument_list|()
argument_list|)
decl_stmt|;
block|}
end_interface

end_unit

